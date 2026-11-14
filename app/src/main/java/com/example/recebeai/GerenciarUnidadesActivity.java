
package com.example.recebeai;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GerenciarUnidadesActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText editBlockName;
    private EditText editApartmentNumber;

    private Button buttonAddBlock;
    private Button buttonAddApartment;
    private Button buttonBack;

    private LinearLayout blocksContainer;
    private LinearLayout apartmentsContainer;

    private TextView textBlocksEmpty;
    private TextView textApartmentsEmpty;

    private Spinner spinnerBlocks;

    private ArrayList<String> blockIds;
    private ArrayList<String> blockNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.enableEdgeToEdge(getWindow());

        setContentView(R.layout.activity_gerenciar_unidades);

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

        editBlockName = findViewById(R.id.editBlockName);
        editApartmentNumber = findViewById(R.id.editApartmentNumber);

        buttonAddBlock = findViewById(R.id.buttonAddBlock);
        buttonAddApartment = findViewById(R.id.buttonAddApartment);
        buttonBack = findViewById(R.id.buttonBack);

        blocksContainer = findViewById(R.id.blocksContainer);
        apartmentsContainer = findViewById(R.id.apartmentsContainer);

        textBlocksEmpty = findViewById(R.id.textBlocksEmpty);
        textApartmentsEmpty = findViewById(R.id.textApartmentsEmpty);

        spinnerBlocks = findViewById(R.id.spinnerBlocks);

        blockIds = new ArrayList<>();
        blockNames = new ArrayList<>();

        buttonAddBlock.setOnClickListener(v -> cadastrarBloco());
        buttonAddApartment.setOnClickListener(v -> cadastrarApartamento());
        buttonBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null) {
            carregarBlocos();
            carregarApartamentos();
        }
    }

    private void cadastrarBloco() {
        String name = editBlockName.getText().toString().trim();

        if (name.isEmpty()) {
            editBlockName.setError("Informe o nome do bloco.");
            editBlockName.requestFocus();
            return;
        }

        String normalizedName = name.toUpperCase();

        buttonAddBlock.setEnabled(false);

        db.collection("blocks")
                .whereEqualTo("normalizedName", normalizedName)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {
                        buttonAddBlock.setEnabled(true);
                        editBlockName.setError("Esse bloco já está cadastrado.");
                        editBlockName.requestFocus();
                        return;
                    }

                    Map<String, Object> block = new HashMap<>();
                    block.put("name", name);
                    block.put("normalizedName", normalizedName);
                    block.put("active", true);
                    block.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("blocks")
                            .add(block)
                            .addOnSuccessListener(documentReference -> {
                                buttonAddBlock.setEnabled(true);
                                editBlockName.setText("");

                                Toast.makeText(
                                        this,
                                        "Bloco cadastrado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                carregarBlocos();
                            })
                            .addOnFailureListener(e -> {
                                buttonAddBlock.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Erro ao cadastrar bloco: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    buttonAddBlock.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Erro ao verificar bloco: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarBlocos() {
        db.collection("blocks")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    blocksContainer.removeAllViews();
                    blockIds.clear();
                    blockNames.clear();

                    if (querySnapshot.isEmpty()) {
                        textBlocksEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textBlocksEmpty.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String blockId = document.getId();
                            String name = document.getString("name");

                            if (name == null) {
                                continue;
                            }

                            blockIds.add(blockId);
                            blockNames.add(name);

                            adicionarBlocoNaTela(blockId, name);
                        }
                    }

                    atualizarSpinner();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao carregar blocos: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void adicionarBlocoNaTela(String blockId, String name) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(
                android.R.drawable.list_selector_background
        );

        TextView textView = new TextView(this);
        textView.setText(name);
        textView.setTextSize(17);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button editButton = criarBotaoAcao("Editar");
        Button disableButton = criarBotaoAcao("Desativar");

        editButton.setOnClickListener(v -> editarBloco(blockId, name));
        disableButton.setOnClickListener(v -> confirmarDesativacaoBloco(
                blockId,
                name
        ));

        actions.addView(editButton);
        actions.addView(disableButton);

        card.addView(textView);
        card.addView(actions);

        blocksContainer.addView(card);
    }

    private void atualizarSpinner() {
        ArrayList<String> options = new ArrayList<>();

        if (blockNames.isEmpty()) {
            options.add("Cadastre um bloco primeiro");
            buttonAddApartment.setEnabled(false);
        } else {
            options.add("Selecione um bloco");
            options.addAll(blockNames);
            buttonAddApartment.setEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerBlocks.setAdapter(adapter);
    }

    private void cadastrarApartamento() {
        String number = editApartmentNumber.getText().toString().trim();

        if (number.isEmpty()) {
            editApartmentNumber.setError("Informe o número do apartamento.");
            editApartmentNumber.requestFocus();
            return;
        }

        int selectedPosition = spinnerBlocks.getSelectedItemPosition();

        if (selectedPosition <= 0 || blockIds.isEmpty()) {
            Toast.makeText(
                    this,
                    "Selecione um bloco.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String blockId = blockIds.get(selectedPosition - 1);
        String blockName = blockNames.get(selectedPosition - 1);
        String normalizedNumber = number.toUpperCase();

        buttonAddApartment.setEnabled(false);

        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String existingNumber =
                                document.getString("normalizedNumber");

                        Boolean active = document.getBoolean("active");

                        if (normalizedNumber.equals(existingNumber)
                                && Boolean.TRUE.equals(active)) {

                            buttonAddApartment.setEnabled(true);

                            editApartmentNumber.setError(
                                    "Esse apartamento já existe nesse bloco."
                            );

                            editApartmentNumber.requestFocus();
                            return;
                        }
                    }

                    Map<String, Object> apartment = new HashMap<>();

                    apartment.put("number", number);
                    apartment.put("normalizedNumber", normalizedNumber);
                    apartment.put("blockId", blockId);
                    apartment.put("blockName", blockName);
                    apartment.put("active", true);
                    apartment.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("apartments")
                            .add(apartment)
                            .addOnSuccessListener(documentReference -> {

                                buttonAddApartment.setEnabled(true);
                                editApartmentNumber.setText("");

                                Toast.makeText(
                                        this,
                                        "Apartamento cadastrado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                carregarApartamentos();
                            })
                            .addOnFailureListener(e -> {
                                buttonAddApartment.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Erro ao cadastrar apartamento: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {
                    buttonAddApartment.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Erro ao verificar apartamento: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void carregarApartamentos() {
        db.collection("apartments")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    apartmentsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        textApartmentsEmpty.setVisibility(View.VISIBLE);
                    } else {
                        textApartmentsEmpty.setVisibility(View.GONE);

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String apartmentId = document.getId();
                            String number = document.getString("number");
                            String blockName = document.getString("blockName");
                            String blockId = document.getString("blockId");

                            if (number == null || blockName == null
                                    || blockId == null) {
                                continue;
                            }

                            adicionarApartamentoNaTela(
                                    apartmentId,
                                    number,
                                    blockName,
                                    blockId
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao carregar apartamentos: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void adicionarApartamentoNaTela(
            String apartmentId,
            String number,
            String blockName,
            String blockId
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(
                android.R.drawable.list_selector_background
        );

        TextView textView = new TextView(this);
        textView.setText("Apartamento " + number + " - " + blockName);
        textView.setTextSize(16);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);

        Button editButton = criarBotaoAcao("Editar");
        Button disableButton = criarBotaoAcao("Desativar");

        editButton.setOnClickListener(v ->
                editarApartamento(apartmentId, number, blockId)
        );

        disableButton.setOnClickListener(v ->
                confirmarDesativacaoApartamento(apartmentId, number, blockName)
        );

        actions.addView(editButton);
        actions.addView(disableButton);

        card.addView(textView);
        card.addView(actions);

        apartmentsContainer.addView(card);
    }

    private Button criarBotaoAcao(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        params.setMargins(4, 8, 4, 0);
        button.setLayoutParams(params);

        return button;
    }

    private void editarBloco(String blockId, String currentName) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(currentName);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Editar bloco")
                .setMessage("Informe o novo nome do bloco.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Informe um nome para o bloco.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    verificarNomeBlocoParaEdicao(blockId, newName);
                })
                .show();
    }

    private void verificarNomeBlocoParaEdicao(
            String blockId,
            String newName
    ) {
        String normalizedName = newName.toUpperCase();

        db.collection("blocks")
                .whereEqualTo("normalizedName", normalizedName)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        if (!document.getId().equals(blockId)) {
                            Toast.makeText(
                                    this,
                                    "Já existe outro bloco com esse nome.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }
                    }

                    atualizarNomeBloco(blockId, newName, normalizedName);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao verificar nome: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void atualizarNomeBloco(
            String blockId,
            String newName,
            String normalizedName
    ) {
        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    WriteBatch batch = db.batch();

                    batch.update(
                            db.collection("blocks").document(blockId),
                            "name",
                            newName
                    );

                    batch.update(
                            db.collection("blocks").document(blockId),
                            "normalizedName",
                            normalizedName
                    );

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        batch.update(
                                document.getReference(),
                                "blockName",
                                newName
                        );
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Bloco atualizado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                carregarBlocos();
                                carregarApartamentos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    "Erro ao atualizar bloco: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao consultar apartamentos: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void confirmarDesativacaoBloco(
            String blockId,
            String blockName
    ) {
        new AlertDialog.Builder(this)
                .setTitle("Desativar bloco")
                .setMessage(
                        "Deseja desativar o " + blockName + "?"
                                + "\n\nO bloco não poderá receber novos apartamentos."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desativar", (dialog, which) ->
                        desativarBloco(blockId)
                )
                .show();
    }

    private void desativarBloco(String blockId) {
        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Desative primeiro os apartamentos desse bloco.",
                                Toast.LENGTH_LONG
                        ).show();
                        return;
                    }

                    db.collection("blocks")
                            .document(blockId)
                            .update("active", false)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Bloco desativado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                carregarBlocos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    "Erro ao desativar bloco: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao verificar apartamentos: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void editarApartamento(
            String apartmentId,
            String currentNumber,
            String blockId
    ) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentNumber);
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Editar apartamento")
                .setMessage("Informe o novo número do apartamento.")
                .setView(input)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newNumber = input.getText().toString().trim();

                    if (newNumber.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Informe o número do apartamento.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    verificarNumeroApartamento(
                            apartmentId,
                            blockId,
                            newNumber
                    );
                })
                .show();
    }

    private void verificarNumeroApartamento(
            String apartmentId,
            String blockId,
            String newNumber
    ) {
        String normalizedNumber = newNumber.toUpperCase();

        db.collection("apartments")
                .whereEqualTo("blockId", blockId)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String existingNumber =
                                document.getString("normalizedNumber");

                        Boolean active = document.getBoolean("active");

                        if (!document.getId().equals(apartmentId)
                                && normalizedNumber.equals(existingNumber)
                                && Boolean.TRUE.equals(active)) {

                            Toast.makeText(
                                    this,
                                    "Já existe esse número nesse bloco.",
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }
                    }

                    db.collection("apartments")
                            .document(apartmentId)
                            .update(
                                    "number", newNumber,
                                    "normalizedNumber", normalizedNumber
                            )
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Apartamento atualizado com sucesso!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                carregarApartamentos();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    "Erro ao atualizar apartamento: "
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao verificar apartamento: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void confirmarDesativacaoApartamento(
            String apartmentId,
            String number,
            String blockName
    ) {
        new AlertDialog.Builder(this)
                .setTitle("Desativar apartamento")
                .setMessage(
                        "Deseja desativar o apartamento "
                                + number + " do " + blockName + "?"
                                + "\n\nO registro será preservado no Firestore."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Desativar", (dialog, which) ->
                        desativarApartamento(apartmentId)
                )
                .show();
    }

    private void desativarApartamento(String apartmentId) {
        db.collection("apartments")
                .document(apartmentId)
                .update("active", false)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(
                            this,
                            "Apartamento desativado com sucesso!",
                            Toast.LENGTH_SHORT
                    ).show();

                    carregarApartamentos();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Erro ao desativar apartamento: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }
}
