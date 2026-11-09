# PortalGunClassic 1.20.1 (Forge 47.4.28, Java 17)

Port do PortalGunClassic (iChun, 1.12.2) para Minecraft 1.20.1. **Esta pasta ainda NÃO foi compilada**:
o ambiente onde foi escrita não tem rede, Gradle, JDK 17 nem as bibliotecas do Forge.

## Gerar a JAR (precisa de internet e JDK 17)
1. Copie `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar` do MDK oficial do Forge 1.20.1 (47.4.28) para esta pasta
   (mantenha o `gradle-wrapper.properties` daqui, ou use o do MDK).
2. `./gradlew build`
3. A JAR fica em `build/libs/portalgunclassic-1.20.1-1.0.0.jar`.
Se aparecerem erros de compilação, envie a saída para corrigir.

## Verificado até agora
- `tools/run-portalmath-check.sh`: a matemática do teletransporte portada dá resultado idêntico ao código original
  (108.000 casos, 36 pares de faces).
- Sintaxe Java e JSON conferidos. Nada foi testado dentro do Minecraft.

## Licenças
Código original: LGPL-3.0 (COPYING, COPYING.LESSER). Os sons creditam a Valve; confirme o direito de redistribuí-los antes de publicar a JAR.
