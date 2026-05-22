import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import prettierConfig from 'eslint-config-prettier'

export default [
  // Paths to skip entirely
  { ignores: ['dist/', 'coverage/', 'node_modules/'] },

  // Core JS rules (no-unused-vars, no-undef, no-console, …)
  js.configs.recommended,

  // React-Hooks rules + shared language settings for all source files
  {
    files: ['**/*.{js,jsx}'],
    plugins: {
      'react-hooks': reactHooks,
    },
    languageOptions: {
      globals: { ...globals.browser },
      parserOptions: {
        ecmaFeatures: { jsx: true },
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      // Enforce the Rules of Hooks (must call in same order, only inside components)
      ...reactHooks.configs.recommended.rules,

      // v7 introduced this experimental rule which false-positives on the common
      // useEffect(() => { fetchData() }, [fetchData]) data-fetching pattern
      'react-hooks/set-state-in-effect': 'off',

      // Warn on unused variables; allow names starting with _ as intentional
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
    },
  },

  // Extra globals available in Vitest test files (globals: true in vite.config.js
  // injects these at runtime but ESLint still needs to know about them)
  {
    files: ['src/test/**/*.{js,jsx}'],
    languageOptions: {
      globals: {
        ...globals.browser,
        vi: 'readonly',
        test: 'readonly',
        expect: 'readonly',
        describe: 'readonly',
        beforeEach: 'readonly',
        afterEach: 'readonly',
        beforeAll: 'readonly',
        afterAll: 'readonly',
      },
    },
  },

  // Must be last: turns off ESLint rules that would conflict with Prettier formatting
  prettierConfig,
]
