package bartsy

class Instructions {

    static constraints = {
    }
    
    static belongsTo = [drink : Drinks]
    String instructions
}
