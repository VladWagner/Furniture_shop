package gp.wagner.backend.services;



import gp.wagner.backend.domain.entites.products.Product;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class Indexer {

   /* @PersistenceContext
    private EntityManager entityManager;*/


    //Инициализация hibernate search
    @Transactional
    public void initIndexing() throws InterruptedException {

       /* SearchSession searchSession = Search.session((EntityManager) entityManager);
        searchSession.massIndexer(Product.class).startAndWait();*/


    }


}
