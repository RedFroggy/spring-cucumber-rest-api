module.exports = {
    extends: ['@commitlint/config-conventional'],
    rules: {
        'subject-case': [
            2, 'always',
            ['sentence-case', 'start-case', 'pascal-case', 'lower-case']
        ],
        'scope-case': [
            2, 'always',
            [
                'lower-case', // default
                'camel-case', // camelCase
                'pascal-case' // PascalCase
            ]
        ]
    }
};
