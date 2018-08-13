package ut.com.vp.jira.project;

import org.junit.Test;
import com.vp.jira.project.api.MyPluginComponent;
import com.vp.jira.project.impl.MyPluginComponentImpl;

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