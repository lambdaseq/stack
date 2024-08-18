module.exports = (_env, _options) => {
    // Figure out which type of project we are building
    const projectType = process.env.PROJECT_TYPE || 'app';
    // Return the appropriate configuration based on the project type
    const configFile = {
        app: './tailwind.app.config.js',
        page: './tailwind.page.config.js'
    }[projectType];
    return {
        plugins: {
            tailwindcss: {config: configFile},
            autoprefixer: {},
        }
    };
}