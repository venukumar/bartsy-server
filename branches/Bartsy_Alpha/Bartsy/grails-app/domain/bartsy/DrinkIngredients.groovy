package bartsy

class DrinkIngredients {

    static constraints = {
    }
    
    static belongsTo = [ingredient:Ingredients,order:Orders]
    Date dateCreated
	Date lastUpdated
}
