# Relatório de compilação e testes — Nebula IPTV 1.0.0

Data: 6 de novembro de 2026

## Inconsistência corrigida

A tela de filmes criava uma `View` completa para cada registro da lista no processo principal. Em catálogos grandes, isso provocava congelamento, ANR ou encerramento por falta de memória. Filmes, séries, TV, resultados de pesquisa e episódios agora usam `GridView`/`ListView` com um adaptador que recicla os cartões visíveis. Capas são carregadas em segundo plano, reduzidas antes da decodificação e limitadas a 8 MB de cache.

A página inicial mantém somente até dez cartões por prévia. A inspeção do código confirmou que nenhuma tela de catálogo adiciona todos os itens diretamente ao layout.

## Interface revisada

- Logo orbital, fundo espacial, brilho neon ciano/roxo e assinatura “Seu universo. Sua programação.” reconstruídos como componentes escaláveis.
- Splash com horizonte planetário; conexão dividida; seletor de perfis; home cinematográfica; detalhes; pesquisa e configurações.
- Navegação inferior no smartphone e menu lateral permanente em Android TV.
- Guia de TV em três colunas na TV/paisagem: categorias, canais e painel do programa.
- Grade de 2 colunas no celular em retrato, 3 em paisagem e 6 na TV.
- Player Media3 com painel translúcido, controles neon, toque e D-pad.
- Playlist local adicionada com seletor de arquivo e permissão persistente.
- Login em retrato alterado para uma única rolagem vertical, impedindo que o formulário fique restrito a metade da tela.
- Login em paisagem/TV dividido em 46% para a marca e 54% para o formulário; todos os textos e botões agora medem a largura do próprio painel, eliminando cortes laterais.
- PNG oficial `Nebula IPTV_ logo.png` incorporado sem edição e mantido no splash, login, menus, placeholders e banner da Android TV.
- Hash dos pixels do logo original e do recurso extraído do APK: `68B3DE6253FE73CD4D63A32387184080BC300FE83A2AF2BFC2C931D6B5196962` em ambos.
- Nomes de categorias Xtream carregados das APIs de categorias; IDs desconhecidos são apresentados como “Outros”, nunca como “Categoria 123”.
- Painel vertical de categorias da TV ao vivo e faixa horizontal dos catálogos com rolagem habilitada e sem limite artificial de categorias.
- Medidas compactas para telefone em paisagem: cabeçalho, barra inferior, cartões, detalhes, perfis, configurações e guia de TV.
- Insets das barras do Android aplicados ao contêiner principal para impedir elementos ocultos fora da área útil.
- Rotação tratada por `configChanges`, preservando biblioteca e tela atual sem repetir a seleção de perfil.
- Player em modo imersivo; controles desaparecem automaticamente 3,5 segundos após a reprodução ou a última interação.
- Home substituída por painel de atalhos responsivo, seguindo a hierarquia visual da referência fornecida sem trocar a identidade Neon Nebula.
- Botão de reprodução da prévia ao vivo ancorado dentro do cartão, com imagem ocupando apenas o espaço restante disponível.
- Categoria Favoritos adicionada em TV ao vivo, filmes e séries; remoção dentro desse filtro atualiza a lista imediatamente.
- Persistência de favoritos separada por tipo, identificador e URL, evitando colisões entre bibliotecas e preservando favoritos legados.
- Novo `Logo_Nebula.png` aplicado exclusivamente como ícone normal e redondo do aplicativo, sem modificar a arte.
- Contadores das categorias Favoritos passam a ser recalculados após cada inclusão ou remoção.
- TV ao vivo permite favoritar pelo coração da lista e pelo novo botão no painel do canal, sincronizando lista, categoria e contador.
- Home do smartphone reduzida aos três cards principais; atalhos secundários foram movidos para a navegação inferior.
- Launcher adaptativo com fundo `#070A1C`, evitando a moldura branca aplicada pelo sistema a ícones legados.
- Favoritos ao vivo passam a usar `tipo|id` como chave estável, reconhecendo também as chaves das versões anteriores e confirmando a gravação antes de atualizar os contadores.
- Botão de favorito da lista ao vivo alterado para não disputar foco com a linha do canal.
- Classificação parental testada para `Adultos`, `XXX`, `+18` e `Eróticos`; resultados comuns `Adult Swim` e `Infantil` permanecem liberados.
- Diálogos de criação e verificação do PIN permanecem abertos ao receber valor inválido, exibindo o erro no próprio campo.
- Canal protegido oculta logo, nome e programação no painel e oferece somente a ação `Desbloquear` antes da reprodução.

## Resultado técnico

- Recursos Android (`aapt2`): aprovados.
- Java 8 contra Android API 35: aprovado.
- Media3 1.11.1 com ExoPlayer, HLS e DASH: incorporado ao DEX.
- Conversão DEX com minSdk 23: aprovada.
- Alinhamento ZIP: aprovado.
- Assinaturas de teste APK v1, v2 e v3: aprovadas.
- Manifesto: pacote `com.nebula.iptv`, versionCode 11, versão `1.5.2-test`, minSdk 23 e targetSdk 35.
- Launchers Android/Android TV e importação `nebula://import`: presentes.
- DEX inspecionado com `NebulaCatalogAdapter`, `ImageLoader`, `MainActivity`, Media3, criptografia, QR, PIN e diagnóstico.

## Integridade

- APK: `Nebula-IPTV-v1.5.2-test.apk`.
- O arquivo `classes.dex` foi validado na raiz do APK, conforme exigido pelo Android Package Manager.
- SHA-256: `2ACDE752B29DB6EBF99A0704DAC0D8751E13BC0455F0EB523D3473C98D3BCCAE`.
- Certificado de teste: o mesmo da versão 1.1.0, permitindo instalação por cima e preservação dos dados locais.

## Teste físico pendente

Não havia emulador nem aparelho Android/Android TV conectado ao ambiente de compilação. A instalação, o comportamento com a lista real, codecs específicos do aparelho e overscan de TV precisam ser confirmados no dispositivo do usuário. O APK foi validado estruturalmente e está pronto para esse teste.
