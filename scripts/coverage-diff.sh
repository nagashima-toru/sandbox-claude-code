#!/bin/bash
#
# カバレッジ比較スクリプト
# 2つのカバレッジレポート (coverage-summary.json) を比較して差分を表示
#
# 使用例:
#   ./scripts/coverage-diff.sh frontend/coverage/coverage-before.json frontend/coverage/coverage-summary.json
#

set -euo pipefail

# 引数チェック
if [ $# -ne 2 ]; then
    echo "Usage: $0 <before-coverage.json> <after-coverage.json>"
    echo ""
    echo "Example:"
    echo "  $0 frontend/coverage/coverage-before.json frontend/coverage/coverage-summary.json"
    exit 1
fi

BEFORE_FILE="$1"
AFTER_FILE="$2"

# ファイル存在チェック
if [ ! -f "$BEFORE_FILE" ]; then
    echo "Error: Before file not found: $BEFORE_FILE"
    exit 1
fi

if [ ! -f "$AFTER_FILE" ]; then
    echo "Error: After file not found: $AFTER_FILE"
    exit 1
fi

# jq がインストールされているか確認
if ! command -v jq &> /dev/null; then
    echo "Error: jq is not installed. Please install it with:"
    echo "  brew install jq"
    exit 1
fi

# 色定義
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Coverage Comparison${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""

# 全体カバレッジの比較
echo -e "${YELLOW}Overall Coverage:${NC}"
echo ""

BEFORE_STMTS=$(jq -r '.total.statements.pct' "$BEFORE_FILE")
AFTER_STMTS=$(jq -r '.total.statements.pct' "$AFTER_FILE")
BEFORE_BRANCH=$(jq -r '.total.branches.pct' "$BEFORE_FILE")
AFTER_BRANCH=$(jq -r '.total.branches.pct' "$AFTER_FILE")
BEFORE_FUNCS=$(jq -r '.total.functions.pct' "$BEFORE_FILE")
AFTER_FUNCS=$(jq -r '.total.functions.pct' "$AFTER_FILE")
BEFORE_LINES=$(jq -r '.total.lines.pct' "$BEFORE_FILE")
AFTER_LINES=$(jq -r '.total.lines.pct' "$AFTER_FILE")

# 差分計算
STMTS_DIFF=$(echo "$AFTER_STMTS - $BEFORE_STMTS" | bc)
BRANCH_DIFF=$(echo "$AFTER_BRANCH - $BEFORE_BRANCH" | bc)
FUNCS_DIFF=$(echo "$AFTER_FUNCS - $BEFORE_FUNCS" | bc)
LINES_DIFF=$(echo "$AFTER_LINES - $BEFORE_LINES" | bc)

# 色付き表示（改善は緑、悪化は赤）
print_diff() {
    local diff=$1
    local value=$2

    if (( $(echo "$diff > 0" | bc -l) )); then
        echo -e "${GREEN}${value}% (+${diff}%)${NC}"
    elif (( $(echo "$diff < 0" | bc -l) )); then
        echo -e "${RED}${value}% (${diff}%)${NC}"
    else
        echo "${value}%"
    fi
}

printf "%-15s %15s %15s\n" "Metric" "Before" "After"
echo "───────────────────────────────────────────────"
printf "%-15s %15s " "Statements" "${BEFORE_STMTS}%"
print_diff "$STMTS_DIFF" "$AFTER_STMTS"
printf "%-15s %15s " "Branches" "${BEFORE_BRANCH}%"
print_diff "$BRANCH_DIFF" "$AFTER_BRANCH"
printf "%-15s %15s " "Functions" "${BEFORE_FUNCS}%"
print_diff "$FUNCS_DIFF" "$AFTER_FUNCS"
printf "%-15s %15s " "Lines" "${BEFORE_LINES}%"
print_diff "$LINES_DIFF" "$AFTER_LINES"

echo ""
echo -e "${YELLOW}File-level Changes (improved files only):${NC}"
echo ""

# ファイルごとの比較（改善されたファイルのみ表示）
jq -r 'to_entries | .[] | select(.key != "total") | .key' "$BEFORE_FILE" | while read -r file; do
    # ファイル名を短縮
    short_file=$(echo "$file" | sed 's|.*/sandbox-claude-code/frontend/src/||')

    before_pct=$(jq -r --arg file "$file" '.[$file].lines.pct // 0' "$BEFORE_FILE")
    after_pct=$(jq -r --arg file "$file" '.[$file].lines.pct // 0' "$AFTER_FILE")

    # 差分計算
    if [ "$before_pct" != "null" ] && [ "$after_pct" != "null" ]; then
        diff=$(echo "$after_pct - $before_pct" | bc)

        # 改善されたファイルのみ表示
        if (( $(echo "$diff > 0" | bc -l) )); then
            printf "%-50s %8s%% → " "$short_file" "$before_pct"
            echo -e "${GREEN}${after_pct}% (+${diff}%)${NC}"
        fi
    fi
done

echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
