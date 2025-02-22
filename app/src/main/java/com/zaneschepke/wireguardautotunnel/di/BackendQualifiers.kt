package com.zaneschepke.wireguardautotunnel.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TunnelShell

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Userspace
