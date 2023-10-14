const path = require("path");

class WebpackConfig {
    mode = "development"
    entry = "./app/app.jsx"// входная точка - исходный файл

    //Место нахождения
    output = {
        path: path.resolve(__dirname, "./public"),     // путь к каталогу выходных файлов - папка public
        publicPath: "/public/",
        filename: "bundle.js"       // название создаваемого файла
    }

    //Найстройки конфигурации сервера
    devServer = {
        historyApiFallback: true,

        //Расположение статических файлов. index.html
        static: {
            directory: path.join(__dirname, "/"),
        },
        port: 8081,
        open: true
    }
    module = {
        rules:[   //загрузчик для jsx
            {
                test: /\.jsx?$/, // определяем тип файлов
                exclude: /(node_modules)/,  // исключаем из обработки папку node_modules
                loader: "babel-loader",   // определяем загрузчик
                options:{
                    presets:[ "@babel/preset-react"]    // используемые плагины
                }
            }
        ]
    }
}

module.exports = new WebpackConfig()