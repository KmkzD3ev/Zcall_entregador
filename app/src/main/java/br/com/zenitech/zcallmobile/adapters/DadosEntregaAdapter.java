package br.com.zenitech.zcallmobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.zenitech.zcallmobile.FinalizarEntrega;
import br.com.zenitech.zcallmobile.R;
import br.com.zenitech.zcallmobile.database.DataBaseOpenHelper;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DadosEntregaAdapter extends RecyclerView.Adapter<DadosEntregaAdapter.ViewHolder> {
    private SharedPreferences prefs;
    private Context context;
    private List<DadosEntrega> elementos;

    private EntregasRepositorio entregasRepositorio;
    private SpotsDialog dialog;

    public DadosEntregaAdapter(Context context, List<DadosEntrega> elementos) {
        this.context = context;
        this.elementos = elementos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        //
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();
        criarConexao();
        //
        View empregoView = inflater.inflate(R.layout.item_pedidos, parent, false);

        //
        return new ViewHolder(empregoView);
    }

    @Override
    public void onBindViewHolder(final DadosEntregaAdapter.ViewHolder holder, int position) {

        //
        final DadosEntrega dadosEntrega = elementos.get(position);

        //TITULO
        TextView cliente = holder.tvCliente;
        cliente.setText(dadosEntrega.cliente);

        //CIDADE
        TextView endereco = holder.tvEndereco;
        endereco.setText(String.format("%s - %s", dadosEntrega.endereco, dadosEntrega.localidade));

        holder.imageAlerta.setVisibility(View.GONE);
        holder.imageLupa.setVisibility(View.GONE);
        holder.imageNotificacao.setVisibility(View.GONE);

        if (dadosEntrega.confirmado.equalsIgnoreCase("0")) {
            holder.imageNotificacao.setVisibility(View.VISIBLE);
        }

        if (dadosEntrega.finalizada.equalsIgnoreCase("1")) {
            holder.imageAlerta.setVisibility(View.VISIBLE);
            holder.tvTitClient.setTextColor(Color.parseColor("#ca3134"));
            holder.tvCliente.setTextColor(Color.parseColor("#ca3134"));
            holder.tvTitEndereco.setTextColor(Color.parseColor("#ca3134"));
            holder.tvEndereco.setTextColor(Color.parseColor("#ca3134"));
            holder.rlList.setOnClickListener(v -> msg(dadosEntrega.id_pedido, position));
        } else if (!dadosEntrega.status.equalsIgnoreCase("P")) {
            holder.imageAlerta.setVisibility(View.VISIBLE);
            holder.tvTitClient.setTextColor(Color.parseColor("#ca3134"));
            holder.tvCliente.setTextColor(Color.parseColor("#ca3134"));
            holder.tvTitEndereco.setTextColor(Color.parseColor("#ca3134"));
            holder.tvEndereco.setTextColor(Color.parseColor("#ca3134"));
            holder.rlList.setOnClickListener(v -> msgStatus(dadosEntrega.id_pedido, position, dadosEntrega.status, dadosEntrega.nome_atendente));
        } else {
            holder.imageLupa.setVisibility(View.VISIBLE);
            holder.rlList.setOnClickListener(v -> {
                Intent in = new Intent(context, FinalizarEntrega.class);
                in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra("id_pedido", dadosEntrega.id_pedido);
                in.putExtra("nome_atendente", dadosEntrega.nome_atendente);
                in.putExtra("troco_para", dadosEntrega.troco_para);
                in.putExtra("valor", dadosEntrega.valor);
                in.putExtra("telefone_pedido", dadosEntrega.telefone_pedido);
                in.putExtra("id_cliente", dadosEntrega.id_cliente);
                in.putExtra("cliente", dadosEntrega.cliente);
                in.putExtra("coordCliLat", dadosEntrega.coord_latitude);
                in.putExtra("coordCliLon", dadosEntrega.coord_longitude);
                in.putExtra("apelido", dadosEntrega.apelido);
                in.putExtra("endereco", dadosEntrega.endereco);
                in.putExtra("numero", dadosEntrega.numero);
                in.putExtra("complemento", dadosEntrega.complemento);
                in.putExtra("ponto_referencia", dadosEntrega.ponto_referencia);
                in.putExtra("localidade", dadosEntrega.localidade);
                in.putExtra("produtos", dadosEntrega.produtos);
                in.putExtra("brindes", dadosEntrega.brindes);
                in.putExtra("observacao", dadosEntrega.observacao);
                in.putExtra("forma_pagamento", dadosEntrega.forma_pagamento);
                context.startActivity(in);
            });
        }
    }

    private void msg(String id_pedido, int position) {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //builder.setIcon(R.drawable.ic_loterias_outline);
        //define o titulo
        builder.setTitle("Atenção");
        //define a mensagem
        String msg = "Não foi possível sincronizar a finalização desta entrega. Isso ocorre quando por algum motivo o app não conseguiu conexão com a internet.";
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("Sincronizar", (arg0, arg1) -> {
            //barra de progresso pontos
            dialog.show();
            //
            final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);
            //
            final Call<DadosEntrega> call = iEmpregos.getFinalizarEntrega(
                    prefs.getString("id_empresa", ""),
                    "finalizarentrega",
                    id_pedido,
                    "E",
                    0,
                    0
            );

            call.enqueue(new Callback<DadosEntrega>() {
                @Override
                public void onResponse(Call<DadosEntrega> call, Response<DadosEntrega> response) {
                    if (response.isSuccessful()) {

                        DadosEntrega dados = response.body();

                        if (dados.status.equals("OK")) {
                            //
                            Toast.makeText(context, "Entrega Finalizada!",
                                    Toast.LENGTH_SHORT).show();
                            //
                            entregasRepositorio.excluir(id_pedido);
                            elementos.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, elementos.size());
                            //
                            prefs.edit().putBoolean("atualizarlista", true).apply();
                        }
                    }
                    dialog.dismiss();
                }

                @Override
                public void onFailure(Call<DadosEntrega> call, Throwable t) {
                    //
                    Toast.makeText(context, "Não foi possível sincronizar!",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });

        });
        /*/define um botão como negativo.
        builder.setNegativeButton("Sair", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
        });*/
        //cria o AlertDialog
        AlertDialog alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    private void msgStatus(String id_pedido, int position, String status, String atendente) {

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        //builder.setIcon(R.drawable.ic_info_outline);
        //define o titulo
        builder.setTitle("Atenção");

        String msg;
        //define a mensagem
        if (status.equalsIgnoreCase("C")) {
            msg = "Esta entrega foi Cancelada por " + atendente + " na central!";
        } else if (status.equalsIgnoreCase("E")) {
            msg = "Esta entrega foi marcada como entregue por " + atendente + " na central!";
        } else if (status.equalsIgnoreCase("EM")) {
            msg = "Esta entrega foi repassada para outro entregador por " + atendente + " na central!";
        } else {
            msg = "Esta entrega foi excluída por " + atendente + " na central!";
        }
        builder.setMessage(msg);
        //define um botão como positivo
        builder.setPositiveButton("OK", (arg0, arg1) -> {

            //
            entregasRepositorio.excluir(id_pedido);
            elementos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, elementos.size());
            //
            prefs.edit().putBoolean("atualizarlista", true).apply();

        });
        /*/define um botão como negativo.
        builder.setNegativeButton("Sair", (arg0, arg1) -> {
            //Toast.makeText(InformacoesVagas.this, "negativo=" + arg1, Toast.LENGTH_SHORT).show();
        });*/
        //cria o AlertDialog
        AlertDialog alerta = builder.create();
        //Exibe alerta
        alerta.show();
    }

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        //RelativeLayout rlList;
        CardView rlList;
        LinearLayout llCorpo;
        TextView tvCliente, tvTitClient;
        TextView tvEndereco, tvTitEndereco;
        ImageView imageNotificacao, imageAlerta, imageLupa;

        ViewHolder(View itemView) {
            super(itemView);

            rlList = itemView.findViewById(R.id.rl_list);
            llCorpo = itemView.findViewById(R.id.llCorpo);
            tvTitClient = itemView.findViewById(R.id.tit_list_cliente);
            tvCliente = itemView.findViewById(R.id.list_cliente);
            tvTitEndereco = itemView.findViewById(R.id.tit_list_endereco);
            tvEndereco = itemView.findViewById(R.id.list_endereco);
            imageNotificacao = itemView.findViewById(R.id.imageNotificacao);
            imageAlerta = itemView.findViewById(R.id.imageAlerta);
            imageLupa = itemView.findViewById(R.id.imageLupa);
        }
    }

    //
    private void criarConexao() {
        try {
            //
            DataBaseOpenHelper dataBaseOpenHelper = new DataBaseOpenHelper(context);
            //
            SQLiteDatabase conexao = dataBaseOpenHelper.getWritableDatabase();
            //
            entregasRepositorio = new EntregasRepositorio(conexao);
        } catch (SQLException ex) {
            AlertDialog.Builder dlg = new AlertDialog.Builder(context);
            dlg.setTitle("Erro");
            dlg.setMessage(ex.getMessage());
            dlg.setNeutralButton("OK", null);
            dlg.show();
        }
    }
}
