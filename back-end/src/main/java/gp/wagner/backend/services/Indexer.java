package gp.wagner.backend.services;



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
