package bartsy

class Cocktails {

	long cocktailId
	String name
	String category
	String alcohol
	String glass
	String ingredients
	String instructions
	int price
	String available
	
	static belongsTo =[venue:Venue]
	
	static constraints = {
	}
}
