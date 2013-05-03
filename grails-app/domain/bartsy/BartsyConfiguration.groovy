package bartsy

class BartsyConfiguration {

    static constraints = {
    }
    
    static belongsTo = [role:Role]
    
    String configName
    String value
}
