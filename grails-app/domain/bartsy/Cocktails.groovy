package bartsy

import java.util.Date;

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
	Date dateCreated
	Date lastUpdated
	
	static belongsTo =[venue:Venue]
	
	static constraints = {
	}
}
