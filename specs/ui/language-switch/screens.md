# 言語切り替え 画面仕様

Epic: #284 / 受け入れ条件: [language-switch.feature](../../acceptance/i18n/language-switch.feature) / ADR: [ADR-0002](../../../docs/adr/0002-design-as-requirement.md)

## 決定内容

今のボタンは「現在の言語」を表示し、押すと反対の言語になる。文字と動作が逆で、押す前に結果が分からず、言語が増えると成り立たない。そこで次の形にする（デモで現状と 4 案を比較し決定）。

- **Web 幅（768px 以上）**: 「地球儀アイコン＋現在の言語名＋▾」のボタン。押すと言語の一覧メニューが開く
- **スマホ幅（768px 未満）**: 地球儀アイコンのみのボタン。メニューは Web 幅と同じ
- メニューは言語名をその言語の表記（日本語 / English）で並べ、右にコード（JA / EN）、現在の言語にチェック。国旗は使わない
- 言語は ja / en の 2 つのまま。言語が増えてもボタンの幅は変わらない

## 画面一覧

| 画面 | パス | 位置 | 対象ロール |
| --- | --- | --- | --- |
| ログイン | `/login` | 画面右上（`absolute top-4 right-4`） | 未ログイン |
| メッセージ一覧 | `/` | ヘッダー右、ログアウトボタンの隣 | ADMIN / VIEWER |

どちらも同じ部品（`LanguageMenu`）を置く。位置だけが違うため、Story は一覧のヘッダー内の 1 か所で代表する。

## 言語切り替えボタン

### レイアウト

```text
Web 幅 (>= 768px)                      スマホ幅 (< 768px)

[ 🌐 日本語 ▾ ] [ ログアウト ]         [ 🌐 ] [ ログアウト ]
   │                                      │
   ▼ （押すと開く。右端をボタンに揃える）  ▼
 ┌───────────────┐                      ┌───────────────┐
 │ ✓ 日本語   JA │                      │ ✓ 日本語   JA │
 │   English  EN │                      │   English  EN │
 └───────────────┘                      └───────────────┘
```

### 状態

| 状態 | 対応シナリオ（.feature） | Story ID |
| --- | --- | --- |
| Web 幅・閉じている・日本語 | ログイン画面 / メイン画面の言語切り替えボタンに現在の言語名が表示される | `screens-languageswitch--web-closed-ja` |
| Web 幅・閉じている・English | 英語設定時はボタンに「English」が表示される | `screens-languageswitch--web-closed-en` |
| Web 幅・メニューが開いている | ボタンを押すと言語の一覧メニューが開き、現在の言語にチェックが付く | `screens-languageswitch--web-open` |
| スマホ幅・アイコンのみ | スマホ幅では言語切り替えボタンが地球儀アイコンのみで表示される | `screens-languageswitch--mobile-closed` |
| スマホ幅・メニューが開いている | スマホ幅でもボタンを押すと同じ言語の一覧メニューが開く | `screens-languageswitch--mobile-open` |

挙動のシナリオ（選ぶと切り替わる・キーボード操作・Esc）は見た目の状態ではないため Story を持たない（コンポーネントテストで検証）。
loading / empty / error 状態はない（データ取得を伴わない）。権限による違いもない（全ロール同じ）。

### 使用コンポーネント / トークン

- 部品: `components/ui/button`（`variant="outline"`）、`components/ui/dropdown-menu`（`@radix-ui/react-dropdown-menu`。shadcn 標準）、アイコンは `lucide-react` の `Globe` / `ChevronDown` / `Check`
- トークン: `globals.css` の CSS 変数のみ（`bg-popover` / `text-popover-foreground` / `border` / `accent` / `muted-foreground`）。任意の色・px 値は使わない
- 幅の切り替え: Tailwind の `md`（768px）。スマホ幅は `max-md:` で言語名と ▾ を隠し、ボタンを 40×40px の正方形にする（タッチ領域 44px の目安に近い高さ 40px は既存の `Button` と同じ）

### a11y / data-testid

- ボタンのアクセシブルな名前は固定で「言語 / Language」（言語名の表示有無・現在の言語に依存しない）。メニューを開くと `aria-expanded="true"`
- メニューは `role="menu"`、項目は `menuitemradio`（現在の言語が `aria-checked="true"`）
- キーボード: Enter / Space で開く、↑↓で項目を移動、Enter で選択、Esc で閉じてボタンにフォーカスを戻す
- `data-testid`: `language-switcher`（ボタン。既存の値を維持）、`language-option-ja` / `language-option-en`（メニュー項目）
- 既存の E2E は `language-switcher` のテキストを `🌐 English` と検証している。実装時に新しい表示に合わせて更新する
