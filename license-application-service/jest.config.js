const { pathsToModuleNameMapper } = require('ts-jest');
const { compilerOptions } = require('./tsconfig');


module.exports = {
  transform: {
    '.(ts|tsx)$': ['ts-jest', { 'tsconfig': 'tsconfig.json' }]
  },
  testRegex: '.*\\.test\\.(ts|tsx)$',
  moduleDirectories: ['node_modules', 'src/main/webapp'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'json'],
  setupFilesAfterEnv: ['<rootDir>/jest.setup.tsx'],
  testEnvironment: 'jsdom',
  moduleNameMapper: {
      '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
      '^react-i18next$': '<rootDir>/__mocks__/react-i18next.js',
    },
};
