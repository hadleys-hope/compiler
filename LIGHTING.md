# Подсветка синтаксиса HopeLang (`.hope`)

В репозитории настроена кроссплатформенная подсветка синтаксиса на базе стандарта **TextMate Grammar** (JSON).
Она работает одинаково как в **IntelliJ IDEA / CLion / Android Studio**, так и в **Visual Studio Code**.

---

## Необходимые файлы в проекте

Для работы подсветки в папке `tools/syntax/` должны лежать два файла:

```text
tools/
└── syntax/
    ├── hope.tmLanguage.json   # Описание грамматики, токенов, регулярных выражений и цветов
    └── package.json           # Манифест расширения, связывающий файлы .hope с грамматикой
```

### 1. `tools/syntax/package.json`
```json
{
  "name": "hopelang",
  "displayName": "HopeLang",
  "version": "0.0.1",
  "engines": {
    "vscode": "^1.0.0"
  },
  "contributes": {
    "languages": [
      {
        "id": "hope",
        "aliases": ["Hope", "hope"],
        "extensions": [".hope"]
      }
    ],
    "grammars": [
      {
        "language": "hope",
        "scopeName": "source.hope",
        "path": "./hope.tmLanguage.json"
      }
    ]
  }
}
```

---

## Настройка в IntelliJ IDEA (пошаговый алгоритм)

Если расширение `*.hope` ранее привязывалось вручную через кастомные типы файлов, его нужно освободить, иначе TextMate не сможет перехватить управление.

### Шаг 1. Освобождаем расширение `*.hope`
1. Открой **Settings** (`Ctrl + Alt + S` на Windows / `Cmd + ,` на macOS).
2. Перейди в: **Editor** $\to$ **File Types**.
3. В списке найди созданный ранее кастомный тип (например, `Hope` или `HopeLang source file`).
4. В блоке справа **File name patterns** выбери `*.hope` и нажми **минус `-`** (удалить).
5. Нажми **Apply**.

### Шаг 2. Подключаем TextMate Bundle
1. В том же окне настроек перейди в: **Editor** $\to$ **TextMate Bundles**.
2. В верхнем блоке нажми на плюсик **`+`**.
3. В проводнике выбери папку **`tools/syntax`** из корня проекта.
4. Нажми **OK**.
5. В списке появится бандл **`hopelang`** с синей галочкой.
6. Нажми **Apply** и **OK**.

> 💡 **Проверка:** Открой любой файл `.hope` (например, `main.hope`). Ключевые слова (`def`, `struct`, `on`, `end`), типы (`int`, `real`), строки и функции мгновенно подсветятся в цветах твоей активной темы оформления.

---

## Настройка в Visual Studio Code (VS Code)

Для коллег, работающих в VS Code (например, C++ разработчиков бэкенда и виртуальной машины), папка `tools/syntax` — это уже **готовое полноценное расширение**.

### Способ 1. Установка в профиль (быстро и навсегда)
Достаточно скопировать папку `tools/syntax` в директорию расширений VS Code:

* **Windows:**
  Скопируй папку `tools/syntax` по пути:
  ```text
  %USERPROFILE%\.vscode\extensions\hopelang
  ```
  *(или выполни в PowerShell из корня проекта):*
  ```powershell
  Copy-Item -Recurse -Force tools/syntax "$env:USERPROFILE\.vscode\extensions\hopelang"
  ```

* **Linux / macOS:**
  ```bash
  cp -r tools/syntax ~/.vscode/extensions/hopelang
  ```

После перезапуска VS Code файлы `.hope` будут автоматически распознаваться как язык `Hope` с полной подсветкой синтаксиса.

---

### Способ 2. Установка через сборку VSIX-пакета (профессионально)
Если нужно передать готовый файл коллегам в один клик:
1. В папке `tools/syntax` выполни в терминале:
   ```bash
   npx @vscode/vsce package
   ```
2. Появится файл `hopelang-0.0.1.vsix`.
3. В VS Code нажми `Ctrl + Shift + P` $\to$ введи **Extensions: Install from VSIX...** $\to$ выбери полученный файл.