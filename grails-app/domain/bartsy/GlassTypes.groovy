package bartsy

class GlassTypes {

    static constraints = {
         glassImage(sqlType:'blob')
    }
    
    String glassType
    byte[] glassImage
}
