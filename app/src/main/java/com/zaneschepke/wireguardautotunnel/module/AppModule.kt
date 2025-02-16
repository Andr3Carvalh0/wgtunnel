package com.zaneschepke.wireguardautotunnel.module

import android.content.Context
import com.zaneschepke.wireguardautotunnel.service.notification.NotificationService
import com.zaneschepke.wireguardautotunnel.service.notification.WireGuardNotification
import com.zaneschepke.wireguardautotunnel.service.shortcut.DynamicShortcutManager
import com.zaneschepke.wireguardautotunnel.service.shortcut.ShortcutManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
	@Singleton
	@ApplicationScope
	@Provides
	fun providesApplicationScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope =
		CoroutineScope(SupervisorJob() + defaultDispatcher)

	@Singleton
	@Provides
	fun provideNotificationService(@ApplicationContext context: Context): NotificationService = WireGuardNotification(context)

	@Singleton
	@Provides
	fun provideShortcutManager(@ApplicationContext context: Context, @IoDispatcher ioDispatcher: CoroutineDispatcher): ShortcutManager =
		DynamicShortcutManager(context, ioDispatcher)
}
