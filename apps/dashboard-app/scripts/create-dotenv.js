const fs = require('fs');
const args = require('args-parser')(process.argv);

const sourcePath = `.env.${args.type}`;
const targetPath = '.env';
fs.copyFileSync(sourcePath, targetPath);
