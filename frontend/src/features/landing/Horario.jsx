import { horario } from '../../data/bar'
import Ornamento from '../../components/ui/Ornamento'

export default function Horario() {
  return (
    <section id="horario" className="seccion horario" aria-labelledby="horario-titulo">
      <div className="horario__programa">
        <Ornamento />
        <h2 id="horario-titulo" className="titulo-seccion">Abrimos cuando cae la noche</h2>
        <p className="horario__intro">La última comanda se toma media hora antes del cierre.</p>
        <ol className="horario__lista">
          {horario.map((franja) => (
            <li key={franja.dias} className={`horario__franja ${franja.apertura ? '' : 'horario__franja--cerrado'}`}>
              <span className="horario__dias">{franja.dias}</span>
              <span className="horario__puntos" aria-hidden="true" />
              <span className="horario__horas">
                {franja.apertura ? `${franja.apertura} a ${franja.cierre}` : 'Cerrado'}
              </span>
              <span className="horario__nota">{franja.nota}</span>
            </li>
          ))}
        </ol>
      </div>
    </section>
  )
}
