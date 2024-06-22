import './CategoriesTile.css'
import {CategoryDto} from "../../api/dto/request/categories/categoryDto";
import {getCorrectStaticFilePath, getRandom, getRightNumDeclension} from "../../infrastrucutre/utils";
import cn from "classnames";
import {useStoreStateSelector} from "../../hooks/useStoreStateSelector";
import {useActions} from "../../hooks/useActions";
import {useEffect} from "react";
import {Link} from "react-router-dom";

const React = require('react')

function CategoriesTile() {

    /*let categoriesTileList = [
      new CategoryDto({id: 1, categoryName: "Кухня", productsAmount: getRandom(20,1500),
      image: "C:/tomcat10/uploads/categories_images/kitchens.jpg"}),
      new CategoryDto({id: 2,  categoryName: "Спальня",productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 3,  categoryName: "Ванная",productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 4,  categoryName: "Гостинная", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 5,  categoryName: "Пуфы", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 6,  categoryName: "Диван-кровати", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 7,  categoryName: "Офис", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 8,  categoryName: "Зеркала", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 9,  categoryName: "Комоды", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 10, categoryName: "Матрасы", productsAmount: getRandom(20,1500),
          image: "C:/tomcat10/uploads/categories_images/mattresses.jpg"}),
      new CategoryDto({id: 11, categoryName: "Столы", productsAmount: getRandom(20,1500)}),
      new CategoryDto({id: 12, categoryName: "Диваны", productsAmount: getRandom(20,1500),
          image: "C:/tomcat10/uploads/categories_images/sofas.jpg"}),
    ];*/

    let {categoriesTileList} = useStoreStateSelector(state => state.categories);
    let {loadCategoriesTile} = useActions();

    useEffect(() =>{
        console.log('Рендеринг компонента вывода плитки категорий')
        loadCategoriesTile()
    }, [])


    return <div className="tile-container">
        <div className="tile-list">
            {
                !categoriesTileList ? null :
                    categoriesTileList.map((c) => {
                        let productsCountStr = c.productsAmount > 0 ? `${c.productsAmount} ${getRightNumDeclension(c.productsAmount, "товар", "товара", "товаров")}` : "";
                        return c.productsAmount > 0 ? <Link to={`/products-by-category/${c.id}`} className="category-tile-item" key={c.id}>
                            <div className="tile-item-products-count">
                                <span>{productsCountStr}</span>
                            </div>
                            <div className={cn("tile-item-category-info", !c.img ? "no-img" : "")}>
                                {
                                   c.img ? <div className="tile-item-category-img"><img src={getCorrectStaticFilePath(c.img)} alt=""/></div> :
                                       <div className="img-replacement"><span>{c.categoryName[0]}</span></div>
                                }
                                <div className="tile-item-category-name">{c.categoryName}</div>
                            </div>
                        </Link> : "";
                    })
            }
        </div>
    </div>
}

export default CategoriesTile