package bartsy

class Ingredients {

    static constraints = {
    }
    
    static belongsTo = [venue:Venue]
    
    String ingredientName
    String brand
    String availability
    String extraCost
}
