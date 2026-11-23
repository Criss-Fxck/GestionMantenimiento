import { useState } from 'react';
import { enviarCorreo } from '../api/emailApi.js';
import Toast from '../components/Toast.jsx';

const inicial = { destinatario: '', asunto: '', cuerpo: '' };

export default function CorreoPage() {
  const [formulario, setFormulario] = useState(inicial);
  const [enviando, setEnviando] = useState(false);
  const [aviso, setAviso] = useState(null);

  function cambiar(evento) { setFormulario((actual) => ({ ...actual, [evento.target.name]: evento.target.value })); }

  async function enviar(evento) {
    evento.preventDefault(); setEnviando(true);
    try {
      const resultado = await enviarCorreo({ destinatario: formulario.destinatario.trim(), asunto: formulario.asunto.trim(), cuerpo: formulario.cuerpo.trim() });
      setAviso({ tipo: 'exito', mensaje: resultado?.mensaje || 'Correo enviado exitosamente.' });
      setFormulario(inicial);
    } catch (error) { setAviso({ tipo: 'error', mensaje: error.message }); }
    finally { setEnviando(false); }
  }

  return <section className="page-section">
    <Toast aviso={aviso} alCerrar={() => setAviso(null)} />
    <div className="page-heading"><div><p className="eyebrow">SERVICIO DE NOTIFICACIONES</p><h1>Correo de prueba</h1><p className="page-description">Envía un correo de prueba usando el servicio configurado en el backend.</p></div></div>
    <div className="table-card email-card"><div className="report-intro"><span className="report-icon"><i className="bi bi-envelope-paper" aria-hidden="true" /></span><div><h2>Nuevo mensaje</h2><p>Los datos de correo se gestionan en el backend; no se guardan en el frontend.</p></div></div>
      <form className="email-form" onSubmit={enviar}><div className="form-field"><label htmlFor="correo-destinatario">Destinatario <span>*</span></label><input id="correo-destinatario" className="form-control" type="email" name="destinatario" maxLength="254" required value={formulario.destinatario} onChange={cambiar} /></div><div className="form-field"><label htmlFor="correo-asunto">Asunto <span>*</span></label><input id="correo-asunto" className="form-control" name="asunto" maxLength="180" required value={formulario.asunto} onChange={cambiar} /></div><div className="form-field"><label htmlFor="correo-cuerpo">Mensaje <span>*</span></label><textarea id="correo-cuerpo" className="form-control" name="cuerpo" rows="7" maxLength="10000" required value={formulario.cuerpo} onChange={cambiar} /></div><div className="modal-actions"><button className="btn btn-primary" type="submit" disabled={enviando}>{enviando && <span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />}{enviando ? 'Enviando…' : <><i className="bi bi-send me-2" aria-hidden="true" />Enviar correo</>}</button></div></form>
    </div>
  </section>;
}
