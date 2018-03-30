package ut.com.devament.concourse;

import org.junit.Test;
import com.devament.concourse.api.MyPluginComponent;
import com.devament.concourse.impl.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
    @Test
    public void testMyName()
    {
        MyPluginComponent component = new MyPluginComponentImpl(null);
        assertEquals("names do not match!", "myComponent",component.getName());
    }
}