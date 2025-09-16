module.exports = {
  root: true,
  env: { es2022: true, node: true },
  parser: "@typescript-eslint/parser",
  parserOptions: { ecmaVersion: "latest", sourceType: "module" },
  plugins: ["@typescript-eslint", "import"],
  extends: ["eslint:recommended", "plugin:@typescript-eslint/recommended", "google"],
  ignorePatterns: ["lib/**", "node_modules/**"],
  rules: {
    // Let us write realistic prompts/strings without failing CI
    "max-len": ["warn", { code: 120, ignoreStrings: true, ignoreTemplateLiterals: true, ignoreComments: true }],
    // Don’t force JSDoc for every function
    "require-jsdoc": "off",
    // Allow empty catch blocks (we deliberately ignore JSON parse errors)
    "no-empty": ["error", { "allowEmptyCatch": true }],
    // If you want warnings instead of errors for 'any', set 'warn' or 'off'
    "@typescript-eslint/no-explicit-any": "off"
  }
};
