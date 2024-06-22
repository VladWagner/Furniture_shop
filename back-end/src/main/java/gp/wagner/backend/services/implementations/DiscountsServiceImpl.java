package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.crud.DiscountRequestDto;
import gp.wagner.backend.domain.entities.categories.Category;
import gp.wagner.backend.domain.entities.products.Discount;
import gp.wagner.backend.domain.entities.products.Product;
import gp.wagner.backend.domain.entities.products.ProductVariant;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.suppliers.DiscountNotFound;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.SortingUtils;
import gp.wagner.backend.infrastructure.enums.sorting.DiscountsSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.products.DiscountsRepository;
import gp.wagner.backend.repositories.products.ProductVariantsRepository;
import gp.wagner.backend.repositories.products.ProductsRepository;
import gp.wagner.backend.services.interfaces.DiscountsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscountsServiceImpl implements DiscountsService {

    @PersistenceContext
    private EntityManager entityManager;

    private DiscountsRepository discountsRepository;

    @Autowired
    public void setPaRepository(DiscountsRepository repository) {
        this.discountsRepository = repository;
    }

    // Репозиторий для вариантов товаров
    private ProductVariantsRepository productVariantsRepository;

    @Autowired
    public void setProductVariantsRepository(ProductVariantsRepository productVariantsRepository) {
        this.productVariantsRepository = productVariantsRepository;
    }

    // Репозиторий для товаров
    private ProductsRepository productsRepository;

    @Autowired
    public void setProductsRepository(ProductsRepository prodRepo) {
        this.productsRepository = prodRepo;
    }

    @Override
    public void create(Discount discount) {
        if (discount == null)
            throw new ApiException("Не удалось создать скидку. В параметрах задано некорректное значение!");

        discountsRepository.saveAndFlush(discount);
    }

    @Override
    public Discount create(DiscountRequestDto dto) {
        if (dto == null || dto.getId() != null)
            throw new ApiException("Не получилось создать объект Discount. Dto задан некорректно!");

        Discount discount = new Discount(null, dto.getPercentage(), dto.getStartsAt(), dto.getEndsAt(), dto.getIsActive(),dto.getIsInfinite());

        // Создать и сохранить скидку для дальнейшего добавления в варианты и товары
        discount = discountsRepository.saveAndFlush(discount);

        if (dto.getProductsVariantsIds() != null && !dto.getProductsVariantsIds().isEmpty())
            addDiscountToPvList(discount.getId(), dto.getProductsVariantsIds());

        // Добавить скидку на все заданные товары
        if (dto.getProductsIds() != null && !dto.getProductsIds().isEmpty())
            addDiscountToProductsList(discount.getId(), dto.getProductsIds());

        // Добавить скидку на всю категорию
        if (dto.getCategoryId() != null)
            addDiscountToCategory(discount.getId(), dto.getCategoryId());


        return discount;
    }

    @Override
    public void update(Discount discount) {
        if (discount == null || discount.getId() == null)
            throw new ApiException("Не удалось изменить запись Discount. Задан некорректный параметр!");

        discountsRepository.saveAndFlush(discount);
    }

    @Override
    public Discount update(DiscountRequestDto dto) {
        if (dto == null || dto.getId() == null)
            throw new ApiException("Не удалось изменить запись Discount. Dto задан некорректно!");

        Discount discount = discountsRepository.findById(dto.getId()).orElseThrow(new DiscountNotFound(dto.getId()));

        // Обработка попытки редактирования неактивной скидки или деактивации активной скидки
        if (!discount.getIsActive() && !dto.getIsActive())
            throw new ApiException("Скидка с id %d неактивна! Неактивные скидки не подлежат редактированию!");
        else if (discount.getIsActive() && !dto.getIsActive()){
            deactivateById(null, discount);
            return discount;
        }
        else if (!discount.getIsActive())
            activateById(discount.getId());

        if(dto.getPercentage() != null)
            discount.setPercentage(dto.getPercentage());

        // Если скидка не бессрочная, тогда задать даты
        if(dto.getStartsAt() != null && !dto.getIsInfinite())
            discount.setStartsAt(dto.getStartsAt());

        if(dto.getEndsAt() != null && !dto.getIsInfinite())
            discount.setEndsAt(dto.getEndsAt());

        // Если скидка была деактивирована и происходит активация
        if (!discount.getIsActive() && dto.getIsActive() && !discount.isExpired())
            discount.setIsActive(dto.getIsActive());

        // Если установлен бесконечный срок действия скидки
        if (!discount.getIsInfinite() && dto.getIsInfinite()){
            discount.setIsInfinite(true);
            discount.setStartsAt(null);
            discount.setEndsAt(null);
        } else if (discount.getIsInfinite() && !dto.getIsInfinite())
            discount.setIsInfinite(false);

        // Сохранить изменения скидки
        discount = discountsRepository.saveAndFlush(discount);

        // Если заданы новые варианты товаров для добавления скидки
        if (dto.getProductsVariantsIds() != null && !dto.getProductsVariantsIds().isEmpty())
            addDiscountToPvList(discount.getId(), dto.getProductsVariantsIds());

        // Если заданы новые товары для добавления скидки
        if (dto.getProductsIds() != null && !dto.getProductsIds().isEmpty())
            addDiscountToProductsList(discount.getId(), dto.getProductsIds());

        // Добавить скидку на всю категорию
        if (dto.getCategoryId() != null)
            addDiscountToCategory(discount.getId(), dto.getCategoryId());

        // Обработать удаление скидки у товаров и категорий

        if (dto.getRemovedVariantsIds() != null && !dto.getRemovedVariantsIds().isEmpty())
            removeDiscountFromPvList(discount.getId(), dto.getProductsVariantsIds());

        // Убрать скидку у заданных товаров
        if (dto.getRemovedProductsIds() != null && !dto.getRemovedProductsIds().isEmpty())
            removeDiscountFromProductsList(discount.getId(), dto.getRemovedProductsIds());

        // Убрать скидку у заданной категории
        if (dto.getRemovedCategoriesIds() != null && !dto.getRemovedCategoriesIds().isEmpty())
            removeDiscountFromCategories(discount.getId(), dto.getRemovedCategoriesIds());

        return discount;
    }

    @Override
    public void deactivateById(Long id, Discount discount) {

        if (id == null && discount == null)
            throw new ApiException("Не удалось деактивировать скидку. Параметры заданы некорректно!");

        //Discount discount = discountsRepository.findById(id).orElseThrow(new DiscountNotFound(id));
        if (discount == null)
            discount = discountsRepository.findById(id).orElseThrow(new DiscountNotFound(id));

        // Получить список id с заданной скидкой
        List<Long> pvIdList = productVariantsRepository.getProductsVariantsIdsWithDiscount(discount.getId());

        if (pvIdList == null || pvIdList.isEmpty())
            return;

        // Задать скидку для вариантов по списку их id
        discountsRepository.deleteDiscountFromPV(discount.getId());

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);

        discount.setIsActive(false);
        discountsRepository.saveAndFlush(discount);
    }


    // Проверять наличие скидок (раз в n-мин) с истёкшим сроком действия и деактивировать их
    //@Scheduled(fixedDelay = 1_800_000)
    // Временная переменная - на проде убрать
    private boolean fistLaunch = true;
    @Scheduled(fixedDelay = 600_000)
    public void deactivateExpiredScheduled() {
        if (fistLaunch){
            fistLaunch = false;
            return;
        }

        List<Discount> discounts = discountsRepository.findAll();

        for (Discount discount : discounts)
            if (discount.isExpired())
                deactivateById(null, discount);

        System.out.printf("\n\tОтработал метод деактивации скидок по расписанию.\n\tПроверено %d скидок\n\n", discounts.size());
    }

    @Override
    public void activateById(long id) {
        Discount discount = discountsRepository.findById(id).orElseThrow(new DiscountNotFound(id));

        if (discount.getIsActive())
            throw new ApiException(String.format("Скидка с id: %d уже активна!", discount.getId()));

        discount.setIsActive(true);
        discountsRepository.saveAndFlush(discount);
    }

    // Добавить скидку к варианту товара
    @Override
    public ProductVariant addDiscountToPv(long discountId, long pvId) {
        if (pvId <= 0)
            throw new ApiException("Добавить скидку для варианта товара не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));

        ProductVariant pv = Services.productVariantsService.getById(pvId);

        if (pv.getDiscount() == null || !pv.getDiscount().isEqual(discount))
            pv.setDiscount(discount);

        productVariantsRepository.saveAndFlush(pv);

        Services.ordersService.recountSumsForVariants(pvId, null);
        Services.basketsService.recountSumsForVariants(pvId, null);
        return pv;
    }

    // Добавить скидку к вариантам товаров по id
    @Override
    public void addDiscountToPvList(long discountId, List<Long> pvIdList) {
        if (pvIdList == null || pvIdList.isEmpty())
            throw new ApiException("Добавить скидку по списку вариантов товаров не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));

        List<ProductVariant> pvList = productVariantsRepository.findProductVariantsByIdList(pvIdList);

        boolean variantsChanged = false;
        for (ProductVariant pv:pvList) {

            // Если у варианта уже задана скидка с таким же % и датами начала и завершения
            if (pv.getDiscount() != null && pv.getDiscount().isEqual(discount))
                continue;
            pv.setDiscount(discount);
            variantsChanged = true;
        }

        // Если ни один вариант не был изменён
        if (!variantsChanged)
            return;

        productVariantsRepository.saveAllAndFlush(pvList);

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);
    }

    // Добавить скидку к товару по id
    @Override
    public Product addDiscountToProduct(long discountId, long productId) {
        if (productId <= 0)
            throw new ApiException("Добавить скидку для варианта товара не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));
        Product product = Services.productsService.getById(productId);

        // Получить все варианты товара
        List<ProductVariant> pvList = productVariantsRepository.findProductVariantsByProductId(productId);

        boolean variantsChanged = false;
        for (ProductVariant pv:pvList) {

            // Если у варианта уже задана скидка с таким же % и датами начала и завершения
            if (pv.getDiscount() != null && pv.getDiscount().isEqual(discount))
                continue;
            pv.setDiscount(discount);
            variantsChanged = true;
        }

        // Если ни один вариант не был изменён
        if (!variantsChanged)
            return product;

        List<Long> pvIdList = pvList.stream().map(ProductVariant::getId).toList();

        productVariantsRepository.saveAllAndFlush(pvList);

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);

        return product;
    }

    // Добавить скидку к вариантам товаров по id самих товаров
    @Override
    public void addDiscountToProductsList(long discountId, List<Long> productsIdsList) {
        if (productsIdsList == null || productsIdsList.isEmpty())
            throw new ApiException("Добавить скидку по списку товаров не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));

        // Получить все варианты всех заданных товаров
        List<ProductVariant> pvList = Services.productVariantsService.getByProductsIds(productsIdsList);

        boolean variantsChanged = false;
        for (ProductVariant pv:pvList) {

            // Если у варианта уже задана скидка с таким же % и датами начала и завершения
            if (pv.getDiscount() != null && pv.getDiscount().isEqual(discount))
                continue;
            pv.setDiscount(discount);
            variantsChanged = true;
        }

        // Если ни один вариант не был изменён
        if (!variantsChanged)
            return;

        List<Long> pvIdList = pvList.stream().map(ProductVariant::getId).toList();

        productVariantsRepository.saveAllAndFlush(pvList);
        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);

    }

    // Добавить скидку к вариантам товаров по id категории
    @Override
    public void addDiscountToCategory(long discountId, long categoryId) {
        if (categoryId <= 0)
            throw new ApiException("Добавить скидку для категории не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));
        List<Long> categoriesIds = ServicesUtils.getChildCategoriesList(categoryId);

        // Получить список id вариантов товаров для пересчёта суммы заказов и корзин
        List<Long> pvIdList = productVariantsRepository.getProductsVariantsIdsByCategoriesIdsList(categoriesIds);

        if (pvIdList == null || pvIdList.isEmpty())
            return;

        // Задать скидку для вариантов по списку их id
        discountsRepository.updatePvDiscountByIdsList(discount.getId(), pvIdList);

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);

    }

    // Убрать скидку у вариантов товаров заданного списка
    @Override
    public void removeDiscountFromPvList(long discountId, List<Long> pvIdList) {
        if (pvIdList == null || pvIdList.isEmpty())
            throw new ApiException("Добавить скидку по списку вариантов товаров не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));

        // Варианты у которых нужно убрать скидку
        List<ProductVariant> pvList = productVariantsRepository.getProductsVariantsWithDiscountInIdsList(discount.getId(), pvIdList);

        boolean variantsChanged = false;
        for (ProductVariant pv:pvList) {

            // Если у варианта уже задана скидка с таким же % и датами начала и завершения
            if (pv.getDiscount() == null)
                continue;
            pv.setDiscount(null);
            variantsChanged = true;
        }

        // Если ни один вариант не был изменён
        if (!variantsChanged)
            return;

        productVariantsRepository.saveAllAndFlush(pvList);

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);
    }

    // Убрать скидку у товаров из заданного списка
    @Override
    public void removeDiscountFromProductsList(long discountId, List<Long> productsIdsList) {
        if (productsIdsList == null || productsIdsList.isEmpty())
            throw new ApiException("Убрать скидку по списку товаров не удалось! Список задан некорректно!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));

        // Получить все варианты всех заданных товаров
        List<ProductVariant> pvList = discountsRepository.getVariantsForProductsWithDiscount(discount.getId(), productsIdsList).orElse(null);

        if (pvList == null)
            return;

        boolean variantsChanged = false;
        for (ProductVariant pv: pvList) {

            // Если у варианта скидка не задана
            if (pv.getDiscount() == null)
                continue;
            pv.setDiscount(null);
            variantsChanged = true;
        }

        // Если ни один вариант не был изменён
        if (!variantsChanged)
            return;

        List<Long> pvIdList = pvList.stream().map(ProductVariant::getId).toList();

        productVariantsRepository.saveAllAndFlush(pvList);
        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);
    }

    // Добавить скидку у категорий из заданного списка
    @Override
    public void removeDiscountFromCategories(long discountId, List<Long> categoriesIds) {
        if (categoriesIds == null || categoriesIds.isEmpty())
            throw new ApiException("Удалить скидку для списка категории не удалось!");

        Discount discount = discountsRepository.findById(discountId).orElseThrow(new DiscountNotFound(discountId));
        List<Long> childCategoriesList = ServicesUtils.getChildCategoriesList(categoriesIds);

        // Получить список id вариантов товаров для пересчёта суммы заказов и корзин
        List<Long> pvIdList = productVariantsRepository.getProductsVariantsIdsByCategoriesIdsListAndDiscount(discount.getId(), childCategoriesList);

        if (pvIdList == null || pvIdList.isEmpty())
            return;

        // Убрать скидку у вариантов по списку их id, которые данную скидку используют
        discountsRepository.removeDiscountFromPvByIdsList(discount.getId(), pvIdList);

        Services.ordersService.recountSumsForVariants(null, pvIdList);
        Services.basketsService.recountSumsForVariants(null, pvIdList);
    }


    @Override
    public Page<Discount> getAll(int pageNum, int limit,
                                 DiscountsSortEnum sortEnum, GeneralSortEnum sortType) {

        if (pageNum > 0)
            pageNum -= 1;

        Pageable pageable = PageRequest.of(pageNum, limit, SortingUtils.createSortForDiscountsSelection(sortEnum, sortType));

        return discountsRepository.findAll(pageable);
    }

    @Override
    public Discount getById(long id) {
        return discountsRepository.findById(id).orElseThrow(new DiscountNotFound(id));
    }

    @Override
    public List<Category> getCategoriesWithDiscount(long discountId) {



        return null;
    }
}
