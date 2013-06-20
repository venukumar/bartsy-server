package bartsy

import java.util.Date;

class Ingredients {

    static constraints = {
    }
    
    static belongsTo = [venue:Venue,category:IngredientCategory]
    
	long ingredientId
    String name
    int price
	String available
	Date dateCreated
	Date lastUpdated
   }
