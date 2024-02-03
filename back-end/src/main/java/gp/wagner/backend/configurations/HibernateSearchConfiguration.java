package gp.wagner.backend.configurations;


import org.springframework.context.annotation.Configuration;


@Configuration
public class HibernateSearchConfiguration  {

    /*@Bean
    public LuceneAnalysisConfigurer luceneAnalysisConfigurer() {
        return context -> {
            context.analyzer("product_analyzer").custom()
                    .tokenizer("standard")
                    .tokenFilter("lowercase")
                    .tokenFilter("asciifolding");
        };
    }*/
}
