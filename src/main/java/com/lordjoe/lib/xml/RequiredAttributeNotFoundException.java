
package com.lordjoe.lib.xml;



/**
* An unknown property was requested
* com.lordjoe.lib.xml.UnknownPropertyException
*/
public class RequiredAttributeNotFoundException extends RuntimeException
{
    public RequiredAttributeNotFoundException(String needed,NameValue[] attributes) {
        super(buildMessage(needed, attributes));
    }
    
    protected static String buildMessage(String needed,NameValue[] attributes)
    {
        StringBuilder sb = new StringBuilder();
        //noinspection StringConcatenationInsideStringBufferAppend
        sb.append("The required attribute \'" + needed + "\' was not found \n" +
                "Found attributes are: ");
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < attributes.length; i++)
        {
            NameValue attribute = attributes[i];
            //noinspection StringConcatenationInsideStringBufferAppend
            sb.append(attribute.getName() + "='" + attribute.getValue() + "', ");
        }
        return sb.toString();
    }
}
