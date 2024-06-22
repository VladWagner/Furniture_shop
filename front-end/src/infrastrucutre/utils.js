import fp from "fingerprintjs2";
import jwt from 'jsonwebtoken';
import {Constants} from "./constants";

//Получение случайного значения
export const getRandom = function(lo,hi) {
    return Math.trunc((hi-lo) * Math.random() + lo);
}

// Получить fp посетителя
export const getFingerprint = async function () {
    const fpStorageName = "fp";

    // Попробовать найти отпечаток в хранилищах
    let fingerprint = localStorage.getItem(fpStorageName) ?? sessionStorage.getItem(fpStorageName);

    if (fingerprint) {
        return fingerprint;
    }

    let components = await fp.getPromise();

    // Получить список значений различных компонентов отпечаткаа
    let values = components.map(c => c.value);

    fingerprint = fp.x64hash128(values.join(''), 31);

    if (!fingerprint)
        return null;

    localStorage.setItem(fpStorageName, fingerprint);
    sessionStorage.setItem(fpStorageName, fingerprint);

    return fingerprint;
}

// Разница между датами >= заданного значения
/**
 * Метод проверяет больше ли разница между числами заданного значения
 * @param dateHi больщая дата для вычитания
 * @param dateLo меньшая дата
 * @param checkNum количество едениц, больше которых должна быть разница
 * @param checkNumUnits единицы измерения разницы
 * **/
export const datesDiffGe = function (dateHi, dateLo, checkNum, checkNumUnits) {

    if (typeof checkNumUnits !== 'string' /*|| typeof checkNum !== 'number'*/)
        return -1;

    let diff = dateHi - dateLo;

    // В зависимости от едениц измерения контрольного значения производим вычисления кол-ва прошедших мс, сек, минут и т.д.
    // По умолчанию - количество прошедших часов
    switch (checkNumUnits) {
        case 'ms':
            return diff >= checkNum
        case 's':
            return diff / 1000 >= checkNum
        case 'm':
            return diff / 1000 / 60 >= checkNum
        case 'd':
            return diff / 1000 / 60 / 60 / 24 >= checkNum
        case 'w':
            return diff / 1000 / 60 / 60 / 24 / 7 >= checkNum
        case 'M':
            return diff / 1000 / 60 / 60 / 24 / 7 / 4 >= checkNum
        default:
            return diff / 1000 / 60 / 60 >= checkNum
    }

}

//Получение определённого cookie по имени
export const getCookieValue = function (cookieName) {
    let documentCookies = document.cookie;
    //Создаём массив cookies
    let cookiesCollection = documentCookies.split(';')
        .filter(s => s.length > 0)
        .map(s => s.startsWith(' ') ? s.substring(1,s.length) : s);

    /*console.log(`Массив: ${cookiesCollection}`);*/

    for (let cookie of cookiesCollection) {
        if (!cookie.startsWith(cookieName))
            continue;

        let ind = cookie.indexOf('=');
        return decodeURIComponent(cookie.substring(ind+1,cookie.length));
    }
}

export const parseJWT = (token) => {
    try {
        return jwt.decode(token, {complete: true})
    } catch (error) {
        console.error('Ошибка  JWT:', error);
        return null;
    }
}

// Получить корректный маршрут для запроса на сервер статических файлов
export const getCorrectStaticFilePath = (path) => {

    if(!path || path.length === 0)
        return "";

    if (path.includes("D:/")) {

        path = path.substring(path.indexOf("D:/")+2);

        path = Constants.driveUploadsRoute.concat(path);
    }else if (path.includes("C:/")) {

        path = path.substring(path.indexOf("C:/")+2);

        if (path.includes("tomcat10/uploads")){
            let dirNameLength = "tomcat10/uploads".length;

            path = path.substring(path.indexOf("tomcat10/uploads")+dirNameLength);
        }

        path = Constants.tomcatUploadsRoute.concat(path);
    }

    return path;
}

// Склонение слова в зависимости от числа перед ним стоящего
export const getRightNumDeclension = (number, one, two, five) => {
    number = Math.abs(number);

    number %= 100;

    // Для значений от 5 до 20 возвращаем значение с окончанием "ов", чтобы избежать некорреткный значений типа 14 товара
    if (number >= 5 && number <= 20)
        return five;

    number %= 10;
    if (number === 1)
        return one;

    if (number >= 2 && number <= 4)
        return two;


    return five;
}

export const uniteProductAndVariantNames = (productName, pvTitle) => {

    if ((!productName || !pvTitle) || (productName.length === 0 || pvTitle.length === 0))
        return null;

    // Если после названия товара не установлена точка
    if (productName[productName.length-1] !== '.')
        productName = productName + '. ';

    // Если 1-я буква находится в нижнем регистре и при этом строка не являеся сирийным номером или абревиатурой
    if (new RegExp("^[a-zа-я]{2}[^.\\-_–]").test(pvTitle))
        pvTitle = pvTitle[0].toUpperCase() + pvTitle.substring(1);

    return productName + pvTitle;
}

// Форматировать строку с числом
export const formatStringFromNum = (number) => {
  return new Intl.NumberFormat('ru-RU').format(number)
}

export const isUserAdmin = (roles) => {

    if (!roles || roles.size <= 0)
        return false;

  return roles.includes("Администратор") ||roles.includes("Редактор");
}