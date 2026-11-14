
package com.example.recebeai;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class GerenciarUsuariosActivity extends AppCompatActivity {

    private static final int MODE_PENDING = 0;
    private static final int MODE_ACTIVE = 1;
    private static final int MODE_DISABLED = 2;

    private FirebaseFirestore db;
    private LinearLayout usersContainer;
    private TextView textEmpty;
    private TextView textPendingTitle;

    private Button buttonShowPending;
    private Button buttonShowActive;
    private Button buttonShowDisabled;

    private int currentMode = MODE_PENDING;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_gerenciar_usuarios);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        db = FirebaseFirestore.getInstance();

        usersContainer = findViewById(R.id.usersContainer);
        textEmpty = findViewById(R.id.textEmpty);
        textPendingTitle = findViewById(R.id.textPendingTitle);

        buttonShowPending = findViewById(R.id.buttonShowPending);
        buttonShowActive = findViewById(R.id.buttonShowActive);
        buttonShowDisabled = findViewById(R.id.buttonShowDisabled);

        buttonShowPending.setOnClickListener(v ->
                alterarModo(MODE_PENDING));

        buttonShowActive.setOnClickListener(v ->
                alterarModo(MODE_ACTIVE));

        buttonShowDisabled.setOnClickListener(v ->
                alterarModo(MODE_DISABLED));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && usersContainer != null) {
            carregarUsuarios();
        }
    }

    private void alterarModo(int modo) {
        currentMode = modo;
        atualizarTitulo();
        carregarUsuarios();
    }

    private void atualizarTitulo() {

        if (currentMode == MODE_PENDING) {
            textPendingTitle.setText("Cadastros pendentes");
            textEmpty.setText("Nenhum cadastro pendente.");
        } else if (currentMode == MODE_ACTIVE) {
            textPendingTitle.setText("Moradores ativos");
            textEmpty.setText("Nenhum morador ativo.");
        } else {
            textPendingTitle.setText("Moradores desativados");
            textEmpty.setText("Nenhum morador desativado.");
        }

        buttonShowPending.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(currentMode == MODE_PENDING
                                ? R.color.blue_primary
                                : R.color.blue_dark)));

        buttonShowActive.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(currentMode == MODE_ACTIVE
                                ? R.color.blue_primary
                                : R.color.blue_dark)));

        buttonShowDisabled.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(currentMode == MODE_DISABLED
                                ? R.color.blue_primary
                                : R.color.blue_dark)));
    }

    private void carregarUsuarios() {

        usersContainer.removeAllViews();
        textEmpty.setVisibility(View.GONE);

        db.collection("users")
                .whereEqualTo("role", "morador")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    int quantidade = 0;

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        Boolean active = document.getBoolean("active");
                        String status = document.getString("status");

                        boolean ativo = Boolean.TRUE.equals(active);
                        boolean desativado =
                                !ativo && "disabled".equals(status);

                        boolean pendente =
                                !ativo
                                        && !"rejected".equals(status)
                                        && !"disabled".equals(status);

                        boolean deveExibir =
                                (currentMode == MODE_PENDING && pendente)
                                        || (currentMode == MODE_ACTIVE && ativo)
                                        || (currentMode == MODE_DISABLED && desativado);

                        if (!deveExibir) {
                            continue;
                        }

                        String nome = document.getString("name");
                        String email = document.getString("email");

                        adicionarCartaoUsuario(
                                document.getId(),
                                nome != null ? nome : "Nome não informado",
                                email != null ? email : "E-mail não informado"
                        );

                        quantidade++;
                    }

                    if (quantidade == 0) {
                        textEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "Não foi possível carregar os usuários.",
                            Toast.LENGTH_LONG).show();
                });
    }

    private void adicionarCartaoUsuario(
            String userId,
            String nome,
            String email) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        GradientDrawable background = new GradientDrawable();
        background.setColor(getColor(R.color.white));
        background.setCornerRadius(dp(12));
        background.setStroke(dp(1), getColor(R.color.text_secondary));

        card.setBackground(background);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(16);
        card.setLayoutParams(cardParams);

        TextView nameText = new TextView(this);
        nameText.setText("Nome: " + nome);
        nameText.setTextColor(getColor(R.color.text_primary));
        nameText.setTextSize(16);
        nameText.setTypeface(null, Typeface.BOLD);

        TextView emailText = new TextView(this);
        emailText.setText("E-mail: " + email);
        emailText.setTextColor(getColor(R.color.text_secondary));
        emailText.setTextSize(14);
        emailText.setPadding(0, dp(8), 0, dp(16));

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.CENTER);

        Button actionButton = new Button(this);
        Button secondaryButton = new Button(this);

        if (currentMode == MODE_PENDING) {

            actionButton.setText("Aprovar");
            actionButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.success)));

            secondaryButton.setText("Rejeitar");
            secondaryButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.error)));

            secondaryButton.setOnClickListener(v ->
                    confirmarAcao(userId, nome, "rejeitar"));

            actionButton.setOnClickListener(v ->
                    abrirSelecaoUnidade(userId, nome));

        } else if (currentMode == MODE_ACTIVE) {

            actionButton.setText("Desativar");
            actionButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.error)));

            actionButton.setOnClickListener(v ->
                    confirmarAcao(userId, nome, "desativar"));

        } else {

            actionButton.setText("Reativar");
            actionButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getColor(R.color.success)));

            actionButton.setOnClickListener(v ->
                    confirmarAcao(userId, nome, "reativar"));
        }

        actionButton.setTextColor(getColor(R.color.white));
        secondaryButton.setTextColor(getColor(R.color.white));

        LinearLayout.LayoutParams actionParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        if (currentMode == MODE_PENDING) {
            actionParams.setMarginEnd(dp(8));
        }

        actionButton.setLayoutParams(actionParams);

        if (currentMode == MODE_PENDING) {

            LinearLayout.LayoutParams secondaryParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            secondaryButton.setLayoutParams(secondaryParams);
            buttonsLayout.addView(secondaryButton);
        }

        buttonsLayout.addView(actionButton);

        card.addView(nameText);
        card.addView(emailText);
        card.addView(buttonsLayout);

        usersContainer.addView(card);
    }

    private void abrirSelecaoUnidade(String userId, String nome) {

        db.collection("blocks")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(blocksSnapshot -> {

                    List<DocumentSnapshot> blocos =
                            blocksSnapshot.getDocuments();

                    if (blocos.isEmpty()) {
                        Toast.makeText(this,
                                "Cadastre um bloco ativo antes de aprovar moradores.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    mostrarDialogoUnidade(userId, nome, blocos);
                })
                .addOnFailureListener(e ->
                        mostrarErro());
    }

    private void mostrarDialogoUnidade(
            String userId,
            String nome,
            List<DocumentSnapshot> blocos) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(12), dp(24), dp(8));

        TextView blockLabel = new TextView(this);
        blockLabel.setText("Selecione o bloco");
        blockLabel.setTextSize(15);
        blockLabel.setTextColor(getColor(R.color.text_primary));

        Spinner blockSpinner = new Spinner(this);

        TextView apartmentLabel = new TextView(this);
        apartmentLabel.setText("Selecione o apartamento");
        apartmentLabel.setTextSize(15);
        apartmentLabel.setTextColor(getColor(R.color.text_primary));
        apartmentLabel.setPadding(0, dp(16), 0, 0);

        Spinner apartmentSpinner = new Spinner(this);

        layout.addView(blockLabel);
        layout.addView(blockSpinner);
        layout.addView(apartmentLabel);
        layout.addView(apartmentSpinner);

        List<String> nomesBlocos = new ArrayList<>();
        nomesBlocos.add("Escolha um bloco");

        for (DocumentSnapshot bloco : blocos) {
            String nomeBloco = bloco.getString("name");
            nomesBlocos.add(nomeBloco != null
                    ? nomeBloco
                    : "Bloco sem nome");
        }

        ArrayAdapter<String> blockAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        nomesBlocos
                );

        blockAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        blockSpinner.setAdapter(blockAdapter);

        List<DocumentSnapshot> apartamentosSelecionados =
                new ArrayList<>();

        List<String> nomesApartamentos = new ArrayList<>();
        nomesApartamentos.add("Escolha um apartamento");

        ArrayAdapter<String> apartmentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        nomesApartamentos
                );

        apartmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        apartmentSpinner.setAdapter(apartmentAdapter);
        apartmentSpinner.setEnabled(false);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Vincular unidade")
                .setMessage("Escolha a unidade de " + nome + ".")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aprovar", null)
                .create();

        blockSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {

                        nomesApartamentos.clear();
                        nomesApartamentos.add("Escolha um apartamento");
                        apartamentosSelecionados.clear();

                        apartmentAdapter.notifyDataSetChanged();
                        apartmentSpinner.setSelection(0);
                        apartmentSpinner.setEnabled(false);

                        if (position == 0) {
                            return;
                        }

                        DocumentSnapshot blocoSelecionado =
                                blocos.get(position - 1);

                        carregarApartamentosDoBloco(
                                blocoSelecionado,
                                nomesApartamentos,
                                apartamentosSelecionados,
                                apartmentAdapter,
                                apartmentSpinner
                        );
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {
                    }
                });

        dialog.setOnShowListener(d -> {

            Button approveButton =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            approveButton.setEnabled(false);

            apartmentSpinner.setOnItemSelectedListener(
                    new android.widget.AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(
                                android.widget.AdapterView<?> parent,
                                View view,
                                int position,
                                long id) {

                            approveButton.setEnabled(
                                    position > 0
                                            && position <= apartamentosSelecionados.size()
                            );
                        }

                        @Override
                        public void onNothingSelected(
                                android.widget.AdapterView<?> parent) {

                            approveButton.setEnabled(false);
                        }
                    });

            approveButton.setOnClickListener(v -> {

                int blockPosition = blockSpinner.getSelectedItemPosition();
                int apartmentPosition =
                        apartmentSpinner.getSelectedItemPosition();

                if (blockPosition <= 0
                        || apartmentPosition <= 0
                        || apartmentPosition > apartamentosSelecionados.size()) {

                    Toast.makeText(this,
                            "Selecione um bloco e um apartamento.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentSnapshot bloco =
                        blocos.get(blockPosition - 1);

                DocumentSnapshot apartamento =
                        apartamentosSelecionados.get(apartmentPosition - 1);

                aprovarMorador(
                        userId,
                        bloco,
                        apartamento,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void carregarApartamentosDoBloco(
            DocumentSnapshot bloco,
            List<String> nomesApartamentos,
            List<DocumentSnapshot> apartamentosSelecionados,
            ArrayAdapter<String> apartmentAdapter,
            Spinner apartmentSpinner) {

        String blockId = bloco.getId();

        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(snapshot -> {

                    nomesApartamentos.clear();
                    nomesApartamentos.add("Escolha um apartamento");
                    apartamentosSelecionados.clear();

                    for (DocumentSnapshot apartamento :
                            snapshot.getDocuments()) {

                        String numero = apartamento.getString("number");

                        if (numero != null) {
                            nomesApartamentos.add(numero);
                            apartamentosSelecionados.add(apartamento);
                        }
                    }

                    apartmentAdapter.notifyDataSetChanged();
                    apartmentSpinner.setSelection(0);
                    apartmentSpinner.setEnabled(
                            !apartamentosSelecionados.isEmpty());

                    if (apartamentosSelecionados.isEmpty()) {
                        Toast.makeText(this,
                                "Este bloco não possui apartamentos ativos.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Não foi possível carregar os apartamentos.",
                                Toast.LENGTH_LONG).show());
    }

    private void aprovarMorador(
            String userId,
            DocumentSnapshot bloco,
            DocumentSnapshot apartamento,
            AlertDialog dialog) {

        String nomeBloco = bloco.getString("name");
        String numeroApartamento = apartamento.getString("number");

        db.collection("users")
                .document(userId)
                .update(
                        "active", true,
                        "status", "approved",
                        "blockId", bloco.getId(),
                        "blockName", nomeBloco,
                        "apartmentId", apartamento.getId(),
                        "apartmentNumber", numeroApartamento
                )
                .addOnSuccessListener(unused -> {

                    dialog.dismiss();

                    concluirAcao(
                            "Morador aprovado e unidade vinculada com sucesso!"
                    );
                })
                .addOnFailureListener(e ->
                        mostrarErro());
    }

    private void confirmarAcao(
            String userId,
            String nome,
            String acao) {

        String titulo;
        String mensagem;

        switch (acao) {
            case "aprovar":
                titulo = "Aprovar cadastro";
                mensagem = "Deseja aprovar o cadastro de " + nome + "?";
                break;

            case "rejeitar":
                titulo = "Rejeitar cadastro";
                mensagem = "Deseja rejeitar o cadastro de " + nome + "?";
                break;

            case "desativar":
                titulo = "Desativar morador";
                mensagem = "Deseja desativar o acesso de " + nome + "?";
                break;

            default:
                titulo = "Reativar morador";
                mensagem = "Deseja reativar o acesso de " + nome + "?";
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) ->
                        executarAcao(userId, acao))
                .show();
    }

    private void executarAcao(String userId, String acao) {

        if ("aprovar".equals(acao) || "reativar".equals(acao)) {

            db.collection("users")
                    .document(userId)
                    .update(
                            "active", true,
                            "status", "approved"
                    )
                    .addOnSuccessListener(unused ->
                            concluirAcao("Morador ativado com sucesso!"))
                    .addOnFailureListener(e ->
                            mostrarErro());

        } else if ("rejeitar".equals(acao)) {

            db.collection("users")
                    .document(userId)
                    .update("status", "rejected")
                    .addOnSuccessListener(unused ->
                            concluirAcao("Cadastro rejeitado."))
                    .addOnFailureListener(e ->
                            mostrarErro());

        } else if ("desativar".equals(acao)) {

            db.collection("users")
                    .document(userId)
                    .update(
                            "active", false,
                            "status", "disabled"
                    )
                    .addOnSuccessListener(unused ->
                            concluirAcao("Morador desativado com sucesso!"))
                    .addOnFailureListener(e ->
                            mostrarErro());
        }
    }

    private void concluirAcao(String mensagem) {

        Toast.makeText(this,
                mensagem,
                Toast.LENGTH_SHORT).show();

        carregarUsuarios();
    }

    private void mostrarErro() {

        Toast.makeText(this,
                "Não foi possível realizar a operação.",
                Toast.LENGTH_LONG).show();
    }

    private int dp(int valor) {
        return (int) (valor * getResources()
                .getDisplayMetrics().density);
    }
}
