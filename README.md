# RecebeAI

Aplicativo Android para gerenciamento de encomendas recebidas e retiradas em condomínios residenciais.

O RecebeAI tem como objetivo organizar o controle de encomendas pela portaria, permitir que moradores acompanhem suas entregas e oferecer à administração uma visão geral dos registros do condomínio.

> **Status do projeto:** em desenvolvimento. Este README diferencia as funcionalidades já implementadas e testadas das que ainda estão planejadas.

## Objetivos

- Registrar encomendas recebidas na portaria.
- Identificar o destinatário por bloco, apartamento e morador.
- Permitir que moradores consultem encomendas aguardando retirada e seu histórico.
- Registrar a retirada de encomendas, incluindo quem realizou a retirada.
- Disponibilizar à administração ferramentas de gerenciamento e consulta.

## Tecnologias

- **Android Studio**
- **Java**
- **XML (layouts com Views/Activities)**
- **Firebase Authentication** — autenticação por e-mail e senha.
- **Cloud Firestore** — armazenamento dos dados do aplicativo.

O banco Firestore foi configurado na região `southamerica-east1`.

## Perfis de acesso

| Perfil | Responsabilidade |
|---|---|
| Administração (`admin`) | Gerenciar usuários e unidades do condomínio e consultar encomendas. |
| Portaria (`portaria`) | Cadastrar encomendas e registrar retiradas. |
| Morador (`morador`) | Consultar encomendas destinadas a si e acompanhar o histórico. |

## Funcionalidades

### Administração

**Implementadas e testadas:**

- Tela inicial de administração.
- Acesso à tela de gerenciamento de usuários.
- Listagem de moradores em três categorias: pendentes, ativos e desativados.
- Aprovação de cadastros de moradores.
- Rejeição de cadastros pendentes.
- Desativação e reativação de moradores.
- Cadastro de blocos e prevenção de nomes duplicados entre blocos ativos.
- Edição do nome de blocos.
- Desativação de blocos, impedida enquanto houver apartamentos ativos vinculados.
- Cadastro de apartamentos vinculados a blocos.
- Prevenção de números de apartamento duplicados dentro do mesmo bloco.
- Edição e desativação de apartamentos, sem excluir seus registros.
- Vínculo do morador a um bloco e apartamento durante a aprovação do cadastro.

**Planejadas:**

- Consultar encomendas pendentes.
- Consultar o histórico de encomendas e retiradas.
- Pesquisar por morador, apartamento, bloco e período.

### Portaria

**Planejadas:**

- Cadastrar encomendas e registrar foto.
- Selecionar bloco e apartamento e identificar o morador destinatário.
- Registrar automaticamente data e horário de recebimento.
- Consultar e pesquisar encomendas pendentes.
- Registrar a retirada e o nome de quem retirou.
- Registrar assinatura digital e foto da pessoa com a encomenda.
- Registrar automaticamente data e horário da retirada.

**Status:** Activity e layout criados; funcionalidades de encomendas ainda não implementadas.

### Morador

**Planejadas:**

- Visualizar encomendas aguardando retirada.
- Consultar fotos, datas e horários das encomendas.
- Consultar o histórico de recebimentos e retiradas.

**Status:** Activity e layout criados; funcionalidades ainda não implementadas.

## O que já foi desenvolvido

- Projeto Android criado com o nome **RecebeAI**.
- Identificador de pacote: `com.example.recebeai`.
- Estrutura inicial em Java e XML.
- Firebase integrado ao projeto.
- Firebase Authentication configurado para login com e-mail e senha.
- Cloud Firestore configurado.
- Login implementado com Firebase Authentication.
- Consulta do documento do usuário no Firestore após autenticação.
- Verificação do perfil (`admin`, `portaria` ou `morador`) e do estado da conta (`active`).
- Redirecionamento implementado de acordo com o perfil.
- Tratamento inicial para credenciais inválidas, perfil ausente, conta desativada e perfil não reconhecido.
- Cadastro de moradores com e-mail e senha pelo Firebase Authentication.
- Criação do documento do morador na coleção `users`, inicialmente com `role = "morador"` e `active = false`, aguardando aprovação administrativa.
- Gerenciamento administrativo dos moradores pendentes, ativos e desativados.
- Aprovação e rejeição de cadastros pendentes.
- Desativação e reativação de contas de moradores.
- Cadastro, edição e desativação de blocos.
- Cadastro, edição e desativação de apartamentos.
- Associação dos apartamentos aos blocos por meio de `blockId`.
- Seleção de bloco e apartamento durante a aprovação do morador.
- Salvamento dos dados da unidade no documento do morador: `blockId`, `blockName`, `apartmentId` e `apartmentNumber`.
- Regras de segurança do Firestore publicadas para as coleções `users`, `blocks` e `apartments`.
- Testes realizados nas telas e operações implementadas até esta etapa.

## Estrutura do projeto

A estrutura principal inclui as seguintes Activities e layouts:

```text
RecebeAI/
├── app/
│   ├── google-services.json
│   └── src/
│       └── main/
│           ├── java/com/example/recebeai/
│           │   ├── MainActivity.java
│           │   ├── AdminActivity.java
│           │   ├── PortariaActivity.java
│           │   ├── MoradorActivity.java
│           │   ├── CadastroActivity.java
│           │   ├── GerenciarUsuariosActivity.java
│           │   └── GerenciarUnidadesActivity.java
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_main.xml
│           │   │   ├── activity_admin.xml
│           │   │   ├── activity_portaria.xml
│           │   │   ├── activity_morador.xml
│           │   │   ├── activity_cadastro.xml
│           │   │   ├── activity_gerenciar_usuarios.xml
│           │   │   └── activity_gerenciar_unidades.xml
│           │   └── values/
│           │       ├── strings.xml
│           │       └── colors.xml
│           └── AndroidManifest.xml
└── README.md
```

A estrutura poderá mudar conforme novas telas e componentes forem adicionados.

## Modelo de dados no Firestore

### Coleção `users`

Os documentos da coleção `users` utilizam o UID do Firebase Authentication como ID do documento.

Exemplo ilustrativo de um morador aprovado e vinculado a uma unidade:

```json
{
  "name": "Nome do morador",
  "email": "morador@exemplo.com",
  "role": "morador",
  "active": true,
  "status": "approved",
  "blockId": "ID_DO_BLOCO",
  "blockName": "Bloco A",
  "apartmentId": "ID_DO_APARTAMENTO",
  "apartmentNumber": "101"
}
```

Moradores recém-cadastrados aguardam aprovação administrativa e são criados inicialmente com `active = false`.

A senha é gerenciada pelo Firebase Authentication e **não deve ser armazenada no Firestore**.

### Coleção `blocks`

Armazena os blocos cadastrados pela administração. Entre os campos utilizados estão:

- `name`
- `normalizedName`
- `active`
- `createdAt`

### Coleção `apartments`

Armazena os apartamentos e sua associação ao bloco. Entre os campos utilizados estão:

- `number`
- `normalizedNumber`
- `blockId`
- `blockName`
- `active`
- `createdAt`

A desativação utiliza o campo `active = false`, preservando os documentos no Firestore.

## Segurança e regras de acesso

As regras publicadas do Firestore contemplam as coleções `users`, `blocks` e `apartments`.

- Usuários autenticados podem ler o próprio documento.
- Administradores ativos podem consultar os documentos de usuários.
- A criação de usuários por administradores é permitida.
- O cadastro próprio de moradores é limitado ao documento do próprio usuário e aos campos previstos para esse fluxo, com `role = "morador"` e `active = false`.
- Administradores ativos podem atualizar ou excluir documentos de outros usuários, mas não o próprio documento.
- Usuários ativos podem ler blocos e apartamentos.
- Apenas administradores ativos podem criar, atualizar ou excluir blocos e apartamentos.
- As demais coleções permanecem negadas pelas regras atuais até que sejam implementadas regras específicas.

As regras deverão ser ampliadas e revisadas à medida que novas funcionalidades e coleções forem adicionadas. Antes de disponibilizar o aplicativo, todas as permissões deverão ser testadas para cada perfil.

## Próximas etapas

1. Implementar o encerramento de sessão (logout), caso ainda não esteja conectado à ação da interface.
2. Desenvolver o cadastro de encomendas pela portaria.
3. Implementar o registro de fotos das encomendas.
4. Implementar a consulta de encomendas pendentes pela portaria.
5. Implementar o processo de retirada, incluindo identificação de quem retirou.
6. Avaliar e implementar assinatura digital e foto da retirada.
7. Desenvolver a consulta de encomendas e o histórico do morador.
8. Desenvolver consultas e filtros do histórico para a administração.
9. Criar e testar as regras de segurança do Firestore para as futuras coleções de encomendas.
10. Testar os fluxos completos com os três perfis.
11. Avaliar a integração de Firebase Storage para fotos e assinaturas.
12. Avaliar Firebase Cloud Messaging (FCM) para notificações aos moradores.

## Observações sobre serviços Firebase

O Firebase Authentication e o Cloud Firestore já estão integrados. O armazenamento de imagens e o envio de notificações ainda não foram implementados. A integração futura com Firebase Storage e Firebase Cloud Messaging dependerá da configuração e dos limites de cobrança/plano disponíveis no projeto Firebase.

## Como executar o projeto

1. Clone este repositório.
2. Abra a pasta do projeto no Android Studio.
3. Configure o Firebase para o seu próprio projeto.
4. Coloque o arquivo `google-services.json` em `app/`.
5. Sincronize o projeto com o Gradle.
6. Execute o aplicativo em um dispositivo ou emulador Android.

**Atenção:** não compartilhe publicamente arquivos de configuração ou credenciais que possam expor informações privadas do projeto. Cada pessoa que configurar o aplicativo deve utilizar sua própria configuração Firebase. Não publique senhas, chaves privadas ou credenciais de acesso.

## Situação atual

O RecebeAI já possui autenticação, cadastro de moradores, gerenciamento administrativo de usuários, gerenciamento de blocos e apartamentos e vínculo de moradores às unidades. Essas funcionalidades foram implementadas e testadas até esta etapa do desenvolvimento.

As funcionalidades de cadastro e acompanhamento de encomendas, retirada, fotos, assinatura e notificações permanecem como próximas etapas.
