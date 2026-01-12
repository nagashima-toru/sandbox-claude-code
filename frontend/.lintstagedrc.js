module.exports = {
  // TypeScript and JavaScript files
  '**/*.{ts,tsx,js,jsx}': (filenames) => {
    const escapedFilenames = filenames.map((filename) => `"${filename}"`).join(' ');
    return [
      `prettier --write ${escapedFilenames}`,
      `next lint --fix --file ${filenames.map((f) => f.replace(/^.*\/frontend\//, '')).join(' --file ')}`,
      'tsc --noEmit',
    ];
  },

  // Other files (JSON, Markdown, CSS, etc.)
  '**/*.{json,md,css,yaml,yml}': (filenames) => {
    const escapedFilenames = filenames.map((filename) => `"${filename}"`).join(' ');
    return [`prettier --write ${escapedFilenames}`];
  },
};
