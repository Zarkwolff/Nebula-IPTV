# Nebula IPTV

Cliente IPTV independente para Android e Android TV, criado a partir da identidade visual fornecida. O projeto não contém canais, credenciais ou conteúdo comercial e não incorpora código ou marcas do IPTV Smarters.

## Implementado

- Login por playlist M3U/M3U8 remota, arquivo M3U local e Xtream Codes (`player_api.php`).
- Portal URL quando o endpoint retorna uma playlist M3U; portais proprietários precisam de API documentada pelo provedor.
- TV ao vivo, filmes, séries e episódios Xtream.
- EPG atual via XMLTV informado em `x-tvg-url`/`url-tvg`, por URL separada ou pelos padrões Xtream `xmltv.php` e `/epg/usuario/senha`.
- Pesquisa global, favoritos, continuar assistindo e catálogo de itens com Catch Up sinalizado pela fonte.
- Player AndroidX Media3 1.11.1 com HLS, DASH, legendas externas, reconexão automática, retomada e atalhos de controle remoto.
- Perfis e credenciais criptografados com AES-GCM e chave protegida pelo Android Keystore; backups do aplicativo ficam desativados.
- Importação por QR Code através de links `nebula://import`, com validação e confirmação antes de salvar.
- Controle parental com PIN derivado por PBKDF2 e bloqueio de conteúdo adulto identificado no catálogo.
- Diagnóstico compartilhável com dispositivo, rede, servidor e último erro, sempre omitindo usuário, senha, PIN e URLs completas.
- Interface responsiva para toque e D-pad, launcher de telefone e Android TV.
- Catálogos virtualizados com reciclagem de cartões, carregamento sob demanda de capas e uso limitado de memória mesmo em listas extensas.
- Interface Neon Nebula reconstruída a partir da arte do designer: abertura espacial, logo orbital vetorial, conexão dividida, perfis, home cinematográfica, guia de TV em três colunas, detalhes, pesquisa, configurações e player sobreposto.
- Fundo espacial desenhado em tempo real, fonte Inter, navegação inferior no celular e menu lateral na TV.
- Logo oficial fornecido incorporado sem alteração de pixels e aplicado ao splash, login, menus, placeholders e banner da Android TV.
- Categorias Xtream resolvidas pelos endpoints oficiais da própria API (`get_live_categories`, `get_vod_categories` e `get_series_categories`), eliminando a exibição de IDs numéricos.
- Categorias roláveis em todas as bibliotecas, com painel vertical no guia ao vivo e faixa horizontal nos catálogos.
- Interface compacta para smartphones em paisagem, respeitando as áreas da barra de status e da navegação do Android.
- Player imersivo com barras do sistema e controles ocultados automaticamente após 3,5 segundos.
- Estado visual preservado durante a rotação, sem reconectar a lista nem reapresentar a seleção de perfis.
- Home reorganizada em blocos responsivos de TV ao vivo, filmes, séries, Catch Up, favoritos e configurações, preservando a identidade Neon Nebula.
- Favoritos disponível como categoria própria em TV ao vivo, filmes e séries, com chaves isoladas por tipo de conteúdo e compatibilidade com os registros anteriores.
- Cartão de programa ao vivo redimensionado para manter o botão “Assistir agora” integralmente dentro do painel em smartphones na horizontal.
- Ícone do aplicativo substituído pelo arquivo oficial `Logo_Nebula.png`, incorporado sem alteração do arquivo de origem.
- Contadores de Favoritos atualizados imediatamente nas categorias de TV, filmes e séries; o painel da TV ao vivo também oferece um botão dedicado para favoritar o canal selecionado.
- Home simplificada para manter somente TV ao vivo, Filmes e Séries no centro; Catch Up, Favoritos e Configurações ficam no rodapé do smartphone.
- Ícone adaptativo configurado com fundo espacial escuro, removendo o aro branco acrescentado pelo launcher sem alterar a arte fornecida.
- Favoritos de TV ao vivo refeitos com chave estável por tipo e identificador, migração das chaves anteriores, gravação confirmada e retorno visual ao usuário.
- Controle parental ampliado para categorias e títulos marcados como Adulto/Adultos, XXX, +18, Erótico, Pornô e outras nomenclaturas usuais; canais protegidos ocultam metadados e exigem PIN para abrir.
- Biblioteca de demonstração com vídeos abertos da Blender Foundation/Google sample bucket.

## Abrir e compilar

Abra a pasta `NebulaIPTV` no Android Studio (JDK 17, Android SDK 35) e execute `assembleDebug`. A configuração Gradle usa Android Gradle Plugin 8.7.3 e Media3 1.11.1 do repositório Google Maven.

## QR Code de configuração

O QR deve conter um link no formato `nebula://import?data=BASE64URL`. O valor `data` é o JSON UTF-8 abaixo codificado como Base64 URL-safe sem preenchimento:

```json
{"mode":"xtream","url":"http://servidor:80","user":"USUARIO","pass":"SENHA","epg":"http://servidor/epg/USUARIO/SENHA"}
```

Também é aceito o formato direto `nebula://import?mode=xtream&server=...&user=...&password=...&epg=...`. O formato Base64 URL-safe é recomendado para evitar problemas com caracteres especiais. O QR contém informações sensíveis e deve ser enviado ao cliente por canal privado.

O APK entregue foi compilado diretamente com as ferramentas oficiais Android Build Tools 35.0.0 e assinado com uma chave de teste. Para publicação, altere `applicationId`, gere uma chave privada de produção, configure uma política de privacidade e faça testes em aparelhos/TVs reais.

## Limitações conhecidas

- O Media3 amplia o suporte a contêineres e streaming adaptativo, mas a decodificação final ainda depende dos codecs disponíveis no aparelho.
- Catch Up varia entre provedores; esta versão identifica e lista canais marcados pela playlist/API, mas não inventa URLs de arquivo quando o provedor não fornece um padrão documentado.
- Capas e logos remotos são carregados somente quando entram na tela e mantidos em um pequeno cache de memória; servidores sem imagem usam a marca Nebula como alternativa.
