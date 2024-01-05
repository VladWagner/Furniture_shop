package gp.wagner.backend.middleware;

import gp.wagner.backend.services.Indexer;
import gp.wagner.backend.services.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//Класс-агрегатор всех статических объектов сервисов,
// что бы не нужно было каждый раз создавать объект бина при необходимости использовать сервис
//Совсем упрощённый посредник, который просто предоставляет доступ к бинам
@Component("Services")
public class Services {

    //Сервис для товаров
    public static ProductsService productsService;

    @Autowired
    public void setProductsService(ProductsService prodService){
        Services.productsService = prodService;
    }

    //Сервис для категорий
    public static CategoriesService categoriesService;

    //Создаём setter, чтобы Spring используя dependency injection создал объект бина
    @Autowired
    public void setCategoriesService(CategoriesService categService){
        Services.categoriesService = categService;
    }

    //Сервис для производителей
    public static ProducersService producersService;

    @Autowired
    public void setProducersService(ProducersService prodService){
        Services.producersService = prodService;
    }

    //Сервис для значений атрибутов товара
    public static AttributeValuesService attributeValuesService;

    @Autowired
    public void setAttributeValuesServiceService(AttributeValuesService attrValuesService){
        Services.attributeValuesService = attrValuesService;
    }

    //Сервис для вариантов исполнения товара
    public static ProductVariantsService productVariantsService;

    @Autowired
    public void setProductVariantsService(ProductVariantsService prodVariantsService){
        Services.productVariantsService = prodVariantsService;
    }

    //Сервис для изображений вариантов исполнения товара
    public static ProductImagesService productImagesService;

    @Autowired
    public void setProductImagesService(ProductImagesService imagesService){
        Services.productImagesService = imagesService;
    }

    //Сервис для загрузки файлов на сервер
    public static FileManageService fileManageService;

    @Autowired
    public void setFileUploadService(FileManageService fileManageService){
        Services.fileManageService = fileManageService;
    }

    //Сервис для подсчёта просмотров каждого товара
    public static ProductViewsService productViewService;

    @Autowired
    public void setProductViewService(ProductViewsService productViewService){
        Services.productViewService = productViewService;
    }

    //Сервис для посетителей
    public static VisitorsService visitorsService;

    @Autowired
    public void setVisitorsService(VisitorsService visitorsService){
        Services.visitorsService = visitorsService;
    }

    //Сервис для корзин
    public static BasketsService basketsService;

    @Autowired
    public void setVisitorsService(BasketsService basketsService){
        Services.basketsService = basketsService;
    }

    //Сервис для заказов
    public static OrdersService ordersService;

    @Autowired
    public void setOrdersService(OrdersService ordersService){
        Services.ordersService = ordersService;
    }

    //Сервис для подсчёта просмотров каждого товара
    public static CategoryViewsService categoryViewsService ;

    @Autowired
    public void setProductViewService(CategoryViewsService categoryViewsService){
        Services.categoryViewsService = categoryViewsService;
    }

    //Сервис для полнотекстового поиска товаров
    public static SearchService searchService ;

    @Autowired
    public void setProductViewService(SearchService searchService){
        Services.searchService = searchService;
    }

    //Сервис для инициализации hibernate search
    public static Indexer indexer;

    @Autowired
    public void setIndexerService(Indexer indexer){
        Services.indexer = indexer;
    }


}

