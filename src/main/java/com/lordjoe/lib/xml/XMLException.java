
package com.lordjoe.lib.xml;
/**
* Thrown when there is trouble with XML
* @author Steve Lewis
*/
public class XMLException extends RuntimeException
{
    private Exception m_Enclosed;
    public XMLException() {
        super();
    }
    public XMLException(String s) {
        super(s);
    }
    public XMLException(String s,Exception ex) {
        super(s);
        m_Enclosed = ex;
    }
    public Exception getEnclosedException()
    {
        return(m_Enclosed);
    }
    
}
