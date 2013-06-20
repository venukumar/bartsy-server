package bartsy

class IngredientCategory {

    static constraints = {
    }
	static belongsTo = [type:IngredientType]
    String category
}
