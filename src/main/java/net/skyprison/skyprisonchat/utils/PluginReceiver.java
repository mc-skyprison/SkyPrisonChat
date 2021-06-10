package net.skyprison.skyprisonchat.utils;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.skyprison.skyprisonchat.SkyPrisonChat;

public class PluginReceiver extends AbstractModule {

	protected final SkyPrisonChat plugin;

	public PluginReceiver(SkyPrisonChat plugin) {
		this.plugin = plugin;
	}

	public Injector createInjector() {
		return Guice.createInjector(this);
	}

	@Override
	protected void configure() {
		this.bind(SkyPrisonChat.class).toInstance(this.plugin);
	}
}

