# RecebeAI

Aplicativo Android para gerenciamento de encomendas recebidas e
retiradas em condomínios residenciais.

O RecebeAI tem como objetivo organizar o controle de encomendas pela
portaria, permitir que moradores acompanhem suas entregas e oferecer à
administração uma visão geral dos registros do condomínio.

> **Status do projeto:** em desenvolvimento. Este README diferencia o
> que já foi implementado do que está planejado para as próximas etapas.

## Objetivos

-   Registrar encomendas recebidas na portaria.
-   Identificar o destinatário por bloco, apartamento e morador.
-   Permitir que moradores consultem encomendas aguardando retirada e
    seu histórico.
-   Registrar a retirada de encomendas, incluindo quem realizou a
    retirada.
-   Disponibilizar à administração ferramentas de gerenciamento e
    consulta.

## Tecnologias

-   **Android Studio**
-   **Java**
-   **XML (layouts com Views/Activities)**
-   **Firebase Authentication** --- autenticação por e-mail e senha.
-   **Cloud Firestore** --- armazenamento dos dados do aplicativo.

O banco Firestore foi configurado na região `southamerica-east1`.

## Perfis de acesso previstos

O aplicativo foi planejado para trabalhar com três perfis:

  -----------------------------------------------------------------------
  Perfil                              Responsabilidade
  ----------------------------------- -----------------------------------
  Administração (`admin`)             Gerenciar usuários e unidades do
                                      condomínio e consultar encomendas.

  Portaria (`portaria`)               Cadastrar encomendas e registrar
                                      retiradas.

   Morador (`morador`)                 Consultar encomendas destinadas a
                                      si e acompanhar o histórico.
  -----------------------------------------------------------------------

## Funcionalidades

### Administração

**Planejadas:** - Gerenciar usuários. - Gerenciar blocos e
apartamentos. - Consultar encomendas pendentes. - Consultar o histórico
de encomendas e retiradas. - Pesquisar por morador, apartamento, bloco e
período. - Desativar o acesso de usuários que não fazem mais parte do
condomínio, preservando o histórico.

**Implementado até o momento:** - Tela inicial de administração
criada. - Estrutura visual inicial com opções para gerenciamento de
usuários, unidades, encomendas pendentes e histórico. - Opção visual
para sair da conta. A ação ainda precisa ser implementada e testada.

### Portaria

**Planejadas:** - Cadastrar encomendas. - Registrar foto da encomenda. -
Informar bloco, apartamento e morador destinatário. - Registrar
automaticamente data e horário de recebimento. - Consultar e pesquisar
encomendas pendentes. - Registrar a retirada e o nome de quem retirou. -
Registrar assinatura digital e foto da pessoa com a encomenda. -
Registrar automaticamente data e horário da retirada.

**Status:** tela/Activity criada; funcionalidades ainda não
implementadas.

### Morador

**Planejadas:** - Visualizar encomendas aguardando retirada. - Consultar
fotos, datas e horários das encomendas. - Consultar o histórico de
recebimentos e retiradas.

**Status:** tela/Activity criada; funcionalidades ainda não
implementadas.

## O que já foi desenvolvido

-   Projeto Android criado com o nome **RecebeAI**.
-   Identificador de pacote: `com.example.recebeai`.
-   Estrutura inicial em Java e XML.
-   Firebase integrado ao projeto.
-   Firebase Authentication configurado para login com e-mail e senha.
-   Cloud Firestore configurado.
-   Coleção `users` criada para armazenar os perfis dos usuários.
-   Documento de usuário de teste criado no Firestore com os campos:
    -   `name`
    -   `email`
    -   `role`
    -   `active`
-   Login implementado com Firebase Authentication.
-   Após autenticar, o aplicativo consulta o documento do usuário no
    Firestore.
-   O aplicativo verifica o perfil (`admin`, `portaria` ou `morador`) e
    o estado da conta (`active`).
-   Redirecionamento implementado de acordo com o perfil.
-   Tratamento inicial para credenciais inválidas, perfil ausente, conta
    desativada e perfil não reconhecido.
-   Activities e layouts iniciais criados:
    -   `MainActivity` --- login.
    -   `AdminActivity` --- administração.
    -   `PortariaActivity` --- portaria.
    -   `MoradorActivity` --- morador.
-   Login de teste com perfil de administrador validado, com abertura da
    tela de administração.

## Estrutura inicial

A estrutura principal desenvolvida até agora inclui:

``` text
RecebeAI/
├── app/
│   ├── google-services.json
│   └── src/
│       └── main/
│           ├── java/com/example/recebeai/
│           │   ├── MainActivity.java
│           │   ├── AdminActivity.java
│           │   ├── PortariaActivity.java
│           │   └── MoradorActivity.java
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_main.xml
│           │   │   ├── activity_admin.xml
│           │   │   ├── activity_portaria.xml
│           │   │   └── activity_morador.xml
│           │   └── values/
│           │       ├── strings.xml
│           │       └── colors.xml
│           └── AndroidManifest.xml
└── README.md
```

A estrutura poderá mudar conforme novas telas e componentes forem
adicionados.

## Modelo inicial de usuário no Firestore

Os documentos da coleção `users` utilizam o UID do Firebase
Authentication como ID do documento.

Exemplo ilustrativo:

``` json
{
  "name": "Administrador",
  "email": "admin@exemplo.com",
  "role": "admin",
  "active": true
}
```

Os valores acima são apenas um exemplo de estrutura. A senha do usuário
é gerenciada pelo Firebase Authentication e **não deve ser armazenada no
Firestore**.

## Segurança e regras de acesso

Foram criadas regras iniciais no Firestore para a coleção `users`. Elas
permitem que um usuário autenticado leia seu próprio documento e que um
administrador ativo consulte os usuários. A criação e a alteração de
usuários também são restritas ao administrador, conforme as regras
atuais.

**Importante:** as regras atuais são iniciais e ainda precisam ser
ampliadas e revisadas para as futuras coleções, como encomendas, blocos
e apartamentos. Antes de disponibilizar o aplicativo, todas as
permissões deverão ser testadas para cada perfil.

## Próximas etapas

1.  Implementar o encerramento de sessão (logout).
2.  Desenvolver o gerenciamento de usuários pela administração.
3.  Desenvolver o cadastro e gerenciamento de blocos e apartamentos.
4.  Implementar o cadastro de encomendas pela portaria.
5.  Implementar consulta de encomendas pendentes e pesquisa.
6.  Implementar o processo de retirada e seus registros.
7.  Desenvolver a consulta de encomendas e o histórico do morador.
8.  Desenvolver consultas e filtros do histórico para a administração.
9.  Revisar e completar as regras de segurança do Firestore.
10. Testar os fluxos completos com os três perfis.
11. Avaliar a integração de Firebase Storage para fotos e assinaturas.
12. Avaliar Firebase Cloud Messaging (FCM) para notificações aos
    moradores.

## Observações sobre serviços Firebase

O Firebase Authentication e o Cloud Firestore já estão integrados. O uso
de armazenamento de imagens e notificações ainda não foi implementado. A
integração futura com Firebase Storage e Firebase Cloud Messaging
dependerá da configuração e dos limites de cobrança/plano disponíveis no
projeto Firebase.

## Como executar o projeto

1.  Clone este repositório.
2.  Abra a pasta do projeto no Android Studio.
3.  Configure o Firebase para o seu próprio projeto.
4.  Coloque o arquivo `google-services.json` em `app/`.
5.  Sincronize o projeto com o Gradle.
6.  Execute o aplicativo em um dispositivo ou emulador Android.

**Atenção:** não compartilhe publicamente arquivos de configuração ou
credenciais que possam expor informações privadas do projeto. Cada
pessoa que configurar o aplicativo deve utilizar sua própria
configuração Firebase.

## Situação atual

O projeto possui a estrutura inicial das três áreas, integração com
Firebase, autenticação, consulta de perfil e direcionamento para a tela
correspondente. A tela de administração já abre após o login de um
usuário administrador de teste.

As operações de gerenciamento de usuários, unidades e encomendas ainda
estão em desenvolvimento.
