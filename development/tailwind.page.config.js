const presetQuick = require("franken-ui/shadcn-ui/preset-quick");
const defaultTheme = require('tailwindcss/defaultTheme')
const {scanClojure} = require('@multiplyco/tailwind-clj');

module.exports = {
    // Configure to scan clojure files for use in server side applications (with hiccup for example)
    content: {
        files: [
            // TODO: Add your own files to scan here
            './src/**/*.{clj,cljs,cljc}'
        ],
        extract: {
            clj: (content) => scanClojure(content),
            cljs: (content) => scanClojure(content),
            cljc: (content) => scanClojure(content)
        }
    },
    theme: {
        extend: {
            fontFamily: {
                sans: ["Inter var", ...defaultTheme.fontFamily.sans],
            },
        },
    },
    plugins: [
        require('@tailwindcss/forms'),
    ],
    presets: [presetQuick()]
}