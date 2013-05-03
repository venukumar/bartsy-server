package bartsy

class DrinksIngredientsMaster {

    static constraints = {
    }
    
    static belongsTo = [drink:Drinks,venue:Venue,ingredient:Ingredients]
    String quantity
    String unit
}
