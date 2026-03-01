---
name: test-story
description: "Determine test types for Story changes and implement tests to prevent E2E bloat. Called from implement-epic Phase 2 Step 4.5 via Task tool."
---

# テスト種別判定・実装スキル

## 概要

Story 実装後にテスト種別を判定・実装するスキル。E2E 肥大化の防止が主目的。

**呼び出し元**: `implement-epic` Phase 2 Step 4.5 から Task ツール経由で呼び出す。

---

## 呼び出し方法

このスキルは **Agent-Callable** です。Task ツール（サブエージェント）経由で呼び出す。
直接 `/test-story` として実行することもできる。

---

## 実行フロー

### Step 0: 仕様ドキュメントの読み込み（必須・最初に実行）

テスト計画が受け入れ条件のシナリオと OpenAPI エンドポイントを網羅するよう、最初に仕様を読み込む。

```bash
# Epic の受け入れ条件を読み込む
ls specs/acceptance/   # Epic に対応するディレクトリを特定
Read specs/acceptance/[機能名]/*.feature

# OpenAPI 仕様を読み込む
Read specs/openapi/openapi.yaml
```

**ファイルが存在しない場合**: 仕様ファイルが見つからない（古い Epic など）場合はスキップして Step 1 へ進む。

**読み込み目的**:
- テスト計画が受け入れ条件の各シナリオを漏れなくカバーしているか確認するため
- テスト計画が OpenAPI 仕様の全エンドポイントを網羅しているか確認するため
- 仕様ドキュメントを読み込まずにテスト計画を立てると、受け入れ条件に存在しないテストケースが作成されたり、受け入れ条件に存在するシナリオが漏れる可能性がある

**この手順をスキップしてはいけない**: 仕様未確認のテスト計画は仕様乖離の原因となり、レビュー時に手戻りが発生しやすくなる。

---

### Step 1: テスト戦略ドキュメントの読み込み

```bash
Read docs/quality/TEST_STRATEGY.md
Read frontend/docs/TEST_STRATEGY.md
Read backend/docs/TEST_STRATEGY.md
```

### Step 2: 変更ファイルのレイヤー別分類

```bash
# 変更されたファイルを確認
git diff --name-only HEAD~1 HEAD
# または引数から取得
```

変更ファイルを以下のレイヤーに分類する:

| レイヤー | ファイルパターン | 推奨テスト種別 |
|---------|---------------|--------------|
| Backend Domain | `domain/model/*.java`, `domain/repository/*.java` | Unit テスト（Pure JUnit5） |
| Backend UseCase | `application/usecase/*.java` | Unit テスト（Mockito） |
| Backend Mapper | `infrastructure/persistence/*.java` | 統合テスト（Testcontainers） |
| Backend Controller | `presentation/controller/*.java` | 統合テスト（MockMvc） |
| Frontend Hook | `src/hooks/*.ts` | Unit テスト（`renderHook`） |
| Frontend Component | `src/components/**/*.tsx` | Component テスト（RTL） |
| Frontend Validation | `src/lib/validations/*.ts` | Unit テスト（Zod スキーマ） |
| Frontend Context | `src/contexts/*.tsx` | Unit テスト（Context Provider） |

### Step 3: E2E 判定フロー

変更内容を見て、以下の質問に順に答える:

**Q1**: バックエンド API とフロントエンドの両方が同時に関与するフローか？
- No → Unit/Component テストで対応（E2E 不要）

**Q2**: Component テストや Backend 統合テストで代替できないか？
- Yes（代替できる） → 代替テストを書く（E2E ではない）

**Q3**: 既存の E2E テストと実質同一のフローか？
- Yes → 既存テストに吸収する（新規 E2E 不要）

**Q4**: 追加後も時間予算（CI 5分以内・テスト数 30件以下）に収まるか？
- No → 他の E2E を削減してから追加する

**全て通過した場合のみ E2E テストを追加する**

### Step 4: テスト実装計画の出力

以下の形式で計画を出力する:

```
## テスト実装計画

### 変更内容のレイヤー分類
| ファイル | レイヤー | 推奨テスト種別 |
|---------|---------|--------------|
| [ファイル名] | [レイヤー] | [Unit/Component/Integration/E2E] |

### テスト件数計画
- Unit テスト: [N]件（新規）
- Component テスト: [N]件（新規）
- Backend 統合テスト: [N]件（新規）
- E2E テスト: [N]件（新規） ← 0件が目標

### E2E 判定結果
[Q1〜Q4 の回答と結論]

### 現在の E2E テスト数
- 変更前: [N]件
- 変更後: [N]件
```

### Step 5: テストの実装（Unit/Component → Backend Integration → E2E の順）

#### E2E 禁止パターンの変換

| 検証内容 | 変換先 | 実装例 |
|---------|--------|--------|
| フォームバリデーション | Unit テスト | Zod スキーマのテスト |
| ボタンの表示/非表示 | Unit テスト | `renderHook(() => usePermission())` |
| レスポンシブレイアウト | Component テスト | `Object.defineProperty(window, 'matchMedia', ...)` |
| i18n テキスト表示 | Component テスト | `LocaleContext.Provider value={{ locale: 'en', ... }}` |
| localStorage 読み書き | Unit テスト | `localStorage.setItem(...)` in jsdom |
| HTTP 403/401 レスポンス | Backend MockMvc | `mockMvc.perform(...).andExpect(status().isForbidden())` |

#### Unit テストの実装例（Frontend Hook）

```typescript
// tests/unit/hooks/usePermission.test.ts
import { renderHook } from '@testing-library/react';
import { AuthContext } from '@/contexts/AuthContext';
import { usePermission } from '@/hooks/usePermission';

it('VIEWER ロールで canCreate が false になる', () => {
  const wrapper = ({ children }: { children: ReactNode }) => (
    <AuthContext.Provider value={{ user: { role: 'VIEWER' }, isLoading: false, setUser: vi.fn() }}>
      {children}
    </AuthContext.Provider>
  );

  const { result } = renderHook(() => usePermission(), { wrapper });
  expect(result.current.canCreate).toBe(false);
});
```

#### Component テストの実装例（i18n）

```typescript
// tests/component/LoginPage.test.tsx
import { render, screen } from '@testing-library/react';
import { LocaleContext } from '@/contexts/LocaleContext';
import { LoginPage } from '@/app/login/page';

it('英語ロケールでログインボタンが "Login" と表示される', () => {
  render(
    <LocaleContext.Provider value={{ locale: 'en', setLocale: vi.fn() }}>
      <LoginPage />
    </LocaleContext.Provider>
  );
  expect(screen.getByTestId('login-submit-button')).toHaveTextContent('Login');
});
```

#### Backend MockMvc テストの実装例（403/401）

```java
@Test
void createMessage_withViewerRole_returns403() throws Exception {
    String viewerToken = obtainTokenFor("viewer", "viewer123");

    mockMvc.perform(post("/api/messages")
            .header("Authorization", "Bearer " + viewerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"code\":\"TEST\",\"content\":\"Test\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(403));
}
```

### Step 6: E2E テスト総数の確認・報告

```bash
# E2E テスト数を確認（CI チェックと同じコマンド）
grep -rE "^(test|it)\(" frontend/tests/e2e/*.spec.ts 2>/dev/null | wc -l
```

上限: **30件以下**（超過する場合は削減対象を特定して報告する）

---

## 出力形式

```
## 🧪 テスト種別判定結果

### 変更内容の分類
[表形式でレイヤーとテスト種別]

### E2E 判定: [追加する/追加しない]
Q1: バックエンド・フロントエンド両層関与 → [Yes/No]
Q2: 代替不可能 → [Yes（E2E必要）/No（代替可能: [代替種別]）]
Q3: 既存 E2E と重複なし → [Yes/No]
Q4: 上限内 → [Yes/No（現在[N]件/上限30件）]

### 実装したテスト
- Unit テスト: [N]件（[ファイル名]）
- Component テスト: [N]件（[ファイル名]）
- Backend 統合テスト: [N]件（[ファイル名]）
- E2E テスト: [N]件（[ファイル名]）

### E2E テスト総数: [変更前] → [変更後] / 上限30件
[⚠️ 超過している場合は警告]
```

---

## E2E 禁止パターン早見表

| ❌ E2E にしてはいけないケース | ✅ 変換先 |
|---------------------------|---------|
| フォームバリデーション（空欄・文字数・パターン） | Unit テスト（Zod スキーマ） |
| ボタンの表示/非表示（usePermission フック） | Unit テスト（renderHook） |
| レスポンシブレイアウト（カード↔テーブル） | Component テスト（matchMedia モック） |
| i18n テキスト表示（日本語/英語） | Component テスト（LocaleContext モック） |
| localStorage 読み書き | Unit テスト（jsdom） |
| debounce の動作 | Unit テスト（vi.useFakeTimers） |
| HTTP 403/401 エラーレスポンス | Backend MockMvc 統合テスト |
| 既存 E2E テストと実質同一フロー | 削除または既存テストで代替 |

---

## 関連ドキュメント

- [全体テスト戦略](../../docs/quality/TEST_STRATEGY.md) - E2E 選定基準・禁止パターン
- [Frontend テスト戦略](../../frontend/docs/TEST_STRATEGY.md) - 判断フロー・実装例
- [Backend テスト戦略](../../backend/docs/TEST_STRATEGY.md) - MockMvc テスト例
- `/implement-epic` - 呼び出し元スキル（Phase 2 Step 4.5）
- `/review-implementation` - E2E 肥大化チェックを含むレビュー
