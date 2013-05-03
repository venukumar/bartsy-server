package bartsy

class Drinks {

    static constraints = {
    }
    
    static belongsTo = [drinkType:DrinkTypes,drinkCategory:DrinkCategories,glassType:GlassTypes]
    
    String drinkName
    
    }
