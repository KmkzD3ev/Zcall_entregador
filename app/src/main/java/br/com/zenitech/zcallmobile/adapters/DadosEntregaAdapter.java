package br.com.zenitech.zcallmobile.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.com.zenitech.zcallmobile.FinalizarEntrega;
import br.com.zenitech.zcallmobile.R;
import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.repositorios.EntregasRepositorio;


public class DadosEntregaAdapter extends RecyclerView.Adapter<DadosEntregaAdapter.ViewHolder> {
    private SharedPreferences prefs;
    private final Context context;
    private final List<DadosEntrega> elementos;

    SQLiteDatabase conexao;

    private final EntregasRepositorio entregasRepositorio;

    public DadosEntregaAdapter(
            Context context,
            List<DadosEntrega> elementos,
            SQLiteDatabase con,
            EntregasRepositorio entregasRepositorio
    ) {
        this.context = context;
        this.elementos = elementos;
        this.conexao = con;
        this.entregasRepositorio = entregasRepositorio;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        prefs = context.getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        View view = inflater.inflate(R.layout.item_pedidos, parent, false);
        return new ViewHolder(view);
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
            holder.rlList.setVisibility(View.GONE);
            holder.ped_finalizado.setVisibility(View.VISIBLE);
            holder.txt_ped_fin.setText(String.format("Pedido %s finalizado\nAguardando sincronismo", dadosEntrega.id_pedido));
        } else if (!dadosEntrega.status.equalsIgnoreCase("P")) {
            holder.imageNotificacao.setVisibility(View.GONE);
            holder.notification.setVisibility(View.VISIBLE);
            holder.toque_remover.setVisibility(View.VISIBLE);

            holder.tvTitClient.setTextSize(1, 10);
            holder.tvCliente.setTextSize(1, 9);

            holder.tvTitClient.setTextColor(Color.parseColor("#ca3134"));
            holder.tvCliente.setTextColor(Color.parseColor("#ca3134"));

            holder.tvTitClient.setText("Cliente | Endereço:");
            holder.tvCliente.setText(String.format("%s | %s", dadosEntrega.cliente, dadosEntrega.endereco));

            holder.tvTitEndereco.setVisibility(View.GONE);
            holder.tvEndereco.setVisibility(View.GONE);

            String msg, status = dadosEntrega.status, atendente = dadosEntrega.nome_atendente;
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
            holder.txt_notificacao.setText(msg);
            holder.rlList.setOnClickListener(v -> {
                entregasRepositorio.excluir(dadosEntrega.id_pedido);
                elementos.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, elementos.size());
                //
                prefs.edit().putBoolean("atualizarlista", true).apply();
            });
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

    @Override
    public int getItemCount() {
        return elementos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayoutCompat rlList, notification, ped_finalizado, toque_remover;
        LinearLayout llCorpo;
        TextView tvCliente, tvTitClient, txt_ped_fin, txt_notificacao, tvEndereco, tvTitEndereco;
        ImageView imageNotificacao, imageAlerta, imageLupa;

        ViewHolder(View itemView) {
            super(itemView);

            toque_remover = itemView.findViewById(R.id.toque_remover);
            txt_notificacao = itemView.findViewById(R.id.txt_notificacao);
            txt_ped_fin = itemView.findViewById(R.id.txt_ped_fin);
            ped_finalizado = itemView.findViewById(R.id.ped_finalizado);
            notification = itemView.findViewById(R.id.notification);
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
}
