package io.aetera.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration(proxyBeanMethods = false)
@EntityScan(basePackages = [GATEWAY_PACKAGE])
@EnableJpaRepositories(basePackages = [GATEWAY_PACKAGE])
class JpaConfig

const val GATEWAY_PACKAGE: String = "io.aetera.gateway"
