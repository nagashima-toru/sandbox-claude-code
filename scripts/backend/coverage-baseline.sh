#!/bin/bash
#
# Backend coverage baseline (JaCoCo + Surefire)
#
# Prints LINE / BRANCH / INSTRUCTION coverage and test counts from backend/target,
# optionally saves them as a baseline and compares them with a saved baseline.
# Use it before a large change (e.g. a major upgrade) and again after, so that
# the measured coverage does not regress even when it stays above the pom.xml thresholds.
#
# Usage:
#   cd backend && ./mvnw test && cd ..
#   ./scripts/backend/coverage-baseline.sh --save <file>      # record the baseline
#   ./scripts/backend/coverage-baseline.sh --compare <file>   # compare with the baseline
#
# Save the baseline to a git-ignored path (e.g. under .epic/ or /tmp).
#
# Exit codes:
#   0  - OK (no regression)
#   1  - LINE or BRANCH coverage is below the baseline
#   2  - Report not found, baseline not found, or invalid arguments

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
JACOCO_CSV="$REPO_ROOT/backend/target/site/jacoco/jacoco.csv"
SUREFIRE_DIR="$REPO_ROOT/backend/target/surefire-reports"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

usage() {
  sed -n '2,19p' "$0" | sed 's/^# \{0,1\}//'
}

SAVE_FILE=""
COMPARE_FILE=""
while [ $# -gt 0 ]; do
  case "$1" in
    --save)
      [ $# -ge 2 ] || { usage; exit 2; }
      SAVE_FILE="$2"
      shift 2
      ;;
    --compare)
      [ $# -ge 2 ] || { usage; exit 2; }
      COMPARE_FILE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      usage
      exit 2
      ;;
  esac
done

if ! command -v jq &> /dev/null; then
  echo "Error: jq is not installed. Please install it with: brew install jq" >&2
  exit 2
fi

if [ ! -f "$JACOCO_CSV" ] || ! ls "$SUREFIRE_DIR"/TEST-*.xml &> /dev/null; then
  echo "Error: coverage reports not found under backend/target." >&2
  echo "Run 'cd backend && ./mvnw test' first." >&2
  exit 2
fi

if [ -n "$COMPARE_FILE" ] && [ ! -f "$COMPARE_FILE" ]; then
  echo "Error: baseline file not found: $COMPARE_FILE" >&2
  exit 2
fi

# Sum "<type>_MISSED <type>_COVERED" over all classes except the ones excluded
# in pom.xml (ApiApplication, presentation/generated/**).
sum_counter() {
  local type=$1
  awk -F, -v type="$type" '
    NR == 1 {
      for (i = 1; i <= NF; i++) col[$i] = i
      next
    }
    $col["PACKAGE"] ~ /presentation\.generated/ { next }
    $col["CLASS"] == "ApiApplication" { next }
    {
      missed += $col[type "_MISSED"]
      covered += $col[type "_COVERED"]
    }
    END { print missed + 0, covered + 0 }
  ' "$JACOCO_CSV"
}

sum_tests() {
  local attr=$1
  local total=0 value
  for f in "$SUREFIRE_DIR"/TEST-*.xml; do
    value=$(grep -m1 -o " $attr=\"[0-9]*\"" "$f" | head -1 | sed 's/[^0-9]//g')
    total=$((total + ${value:-0}))
  done
  echo "$total"
}

read -r LINE_MISSED LINE_COVERED <<< "$(sum_counter LINE)"
read -r BRANCH_MISSED BRANCH_COVERED <<< "$(sum_counter BRANCH)"
read -r INSTR_MISSED INSTR_COVERED <<< "$(sum_counter INSTRUCTION)"
TESTS=$(sum_tests tests)
FAILURES=$(sum_tests failures)
ERRORS=$(sum_tests errors)
SKIPPED=$(sum_tests skipped)

pct() {
  awk -v c="$1" -v m="$2" 'BEGIN { t = c + m; if (t == 0) printf "0.00"; else printf "%.2f", c * 100 / t }'
}

CURRENT=$(jq -n \
  --argjson lm "$LINE_MISSED" --argjson lc "$LINE_COVERED" --arg lp "$(pct "$LINE_COVERED" "$LINE_MISSED")" \
  --argjson bm "$BRANCH_MISSED" --argjson bc "$BRANCH_COVERED" --arg bp "$(pct "$BRANCH_COVERED" "$BRANCH_MISSED")" \
  --argjson im "$INSTR_MISSED" --argjson ic "$INSTR_COVERED" --arg ip "$(pct "$INSTR_COVERED" "$INSTR_MISSED")" \
  --argjson t "$TESTS" --argjson f "$FAILURES" --argjson e "$ERRORS" --argjson s "$SKIPPED" \
  '{
    line: {missed: $lm, covered: $lc, pct: ($lp | tonumber)},
    branch: {missed: $bm, covered: $bc, pct: ($bp | tonumber)},
    instruction: {missed: $im, covered: $ic, pct: ($ip | tonumber)},
    tests: {total: $t, failures: $f, errors: $e, skipped: $s}
  }')

echo "Backend coverage (JaCoCo, excluding ApiApplication and presentation/generated)"
echo "───────────────────────────────────────────────"
printf "%-14s %9s%%  (%s/%s)\n" "LINE" "$(pct "$LINE_COVERED" "$LINE_MISSED")" "$LINE_COVERED" "$((LINE_COVERED + LINE_MISSED))"
printf "%-14s %9s%%  (%s/%s)\n" "BRANCH" "$(pct "$BRANCH_COVERED" "$BRANCH_MISSED")" "$BRANCH_COVERED" "$((BRANCH_COVERED + BRANCH_MISSED))"
printf "%-14s %9s%%  (%s/%s)\n" "INSTRUCTION" "$(pct "$INSTR_COVERED" "$INSTR_MISSED")" "$INSTR_COVERED" "$((INSTR_COVERED + INSTR_MISSED))"
printf "%-14s %9s  (failures=%s, errors=%s, skipped=%s)\n" "TESTS" "$TESTS" "$FAILURES" "$ERRORS" "$SKIPPED"

if [ -n "$SAVE_FILE" ]; then
  mkdir -p "$(dirname "$SAVE_FILE")"
  echo "$CURRENT" > "$SAVE_FILE"
  echo ""
  echo -e "${GREEN}Baseline saved: $SAVE_FILE${NC}"
fi

if [ -z "$COMPARE_FILE" ]; then
  exit 0
fi

echo ""
echo "Comparison with baseline: $COMPARE_FILE"
echo "───────────────────────────────────────────────"
printf "%-14s %10s %10s %10s\n" "Metric" "Baseline" "Current" "Diff"

REGRESSED=0

# compare_metric <label> <key> <fail-on-regression: 1|0>
compare_metric() {
  local label=$1 key=$2 strict=$3
  local base_c base_m base_pct cur_c cur_m cur_pct diff worse
  base_c=$(jq -r ".$key.covered" "$COMPARE_FILE")
  base_m=$(jq -r ".$key.missed" "$COMPARE_FILE")
  cur_c=$(echo "$CURRENT" | jq -r ".$key.covered")
  cur_m=$(echo "$CURRENT" | jq -r ".$key.missed")
  base_pct=$(pct "$base_c" "$base_m")
  cur_pct=$(pct "$cur_c" "$cur_m")
  diff=$(awk -v a="$cur_pct" -v b="$base_pct" 'BEGIN { printf "%+.2f", a - b }')
  # Compare exact ratios (covered/total) so that rounding cannot hide a regression.
  worse=$(awk -v cc="$cur_c" -v cm="$cur_m" -v bc="$base_c" -v bm="$base_m" \
    'BEGIN { print (cc * (bc + bm) < bc * (cc + cm)) ? 1 : 0 }')

  local color="$NC"
  if [ "$worse" = "1" ]; then
    color="$RED"
    if [ "$strict" = "1" ]; then
      REGRESSED=1
    fi
  elif [ "$diff" != "+0.00" ]; then
    color="$GREEN"
  fi
  printf "%-14s %9s%% %9s%% ${color}%9s%%${NC}\n" "$label" "$base_pct" "$cur_pct" "$diff"
}

compare_metric "LINE" line 1
compare_metric "BRANCH" branch 1
compare_metric "INSTRUCTION" instruction 0

BASE_TESTS=$(jq -r '.tests.total' "$COMPARE_FILE")
printf "%-14s %10s %10s %+10d\n" "TESTS" "$BASE_TESTS" "$TESTS" "$((TESTS - BASE_TESTS))"
if [ "$TESTS" -lt "$BASE_TESTS" ]; then
  echo -e "${YELLOW}Warning: the number of tests decreased.${NC}"
fi

echo ""
if [ "$REGRESSED" = "1" ]; then
  echo -e "${RED}Coverage is below the baseline (LINE or BRANCH).${NC}"
  exit 1
fi
echo -e "${GREEN}No coverage regression.${NC}"
