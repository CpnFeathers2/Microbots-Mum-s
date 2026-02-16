package net.runelite.client.plugins.microbot.beescreenshot;

import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class ScreenShotPluginTest
{
	@Test
	public void testEnabledByDefault()
	{
		PluginDescriptor descriptor = ScreenShotPlugin.class.getAnnotation(PluginDescriptor.class);
		assertTrue("Plugin should be enabled by default", descriptor.enabledByDefault());
	}
}
