import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  ElementRef,
  ViewChild,
  AfterViewInit,
} from '@angular/core';
import * as d3 from 'd3';
import { BracketResponse, MatchResponse } from '../../models/match.model';

interface MatchNode {
  match: MatchResponse;
  round: number;
  position: number;
  x: number;
  y: number;
}

/**
 * BracketViewerComponent renders a tournament bracket as an SVG using D3.js.
 * Nodes represent matches; lines connect matches via next_match_id.
 * Colors come from CSS custom properties so the genre theme is respected.
 */
@Component({
  selector: 'app-bracket-viewer',
  standalone: false,
  templateUrl: './bracket-viewer.html',
  styleUrl: './bracket-viewer.scss',
})
export class BracketViewer implements OnChanges, AfterViewInit {
  @Input() bracket: BracketResponse | null = null;
  @ViewChild('svgContainer', { static: true }) svgContainer!: ElementRef<HTMLDivElement>;

  /** Emits the clicked match's id. BYE matches and empty slots are not clickable. */
  @Output() readonly matchClick = new EventEmitter<number>();

  private readonly NODE_W = 180;
  private readonly NODE_H = 60;
  private readonly COL_GAP = 60;
  private readonly ROW_GAP = 20;

  ngAfterViewInit(): void {
    this.render();
  }

  ngOnChanges(): void {
    this.render();
  }

  private render(): void {
    const container = this.svgContainer?.nativeElement;
    if (!container || !this.bracket) return;

    d3.select(container).selectAll('*').remove();

    const rounds = this.bracket.rounds;
    const totalRounds = rounds.length;
    const primary =
      getComputedStyle(document.documentElement).getPropertyValue('--primary').trim() || '#673ab7';
    const accent =
      getComputedStyle(document.documentElement).getPropertyValue('--accent').trim() || '#ff4081';

    // Build node list
    const nodes: MatchNode[] = [];
    const nodeMap = new Map<number, MatchNode>();

    rounds.forEach((round) => {
      const matchCount = round.matches.length;
      round.matches.forEach((match, idx) => {
        const x = (round.roundNumber - 1) * (this.NODE_W + this.COL_GAP);
        const totalHeight = matchCount * (this.NODE_H + this.ROW_GAP) - this.ROW_GAP;
        const y = idx * (this.NODE_H + this.ROW_GAP) + (this.svgHeight(rounds) - totalHeight) / 2;
        const node: MatchNode = { match, round: round.roundNumber, position: idx, x, y };
        nodes.push(node);
        nodeMap.set(match.id, node);
      });
    });

    const svgW = totalRounds * (this.NODE_W + this.COL_GAP) - this.COL_GAP + 40;
    const svgH = this.svgHeight(rounds) + 40;

    const svg = d3
      .select(container)
      .append('svg')
      .attr('viewBox', `0 0 ${svgW} ${svgH}`)
      .attr('preserveAspectRatio', 'xMidYMid meet')
      .style('width', '100%')
      .style('height', 'auto');

    const g = svg.append('g').attr('transform', 'translate(20,20)');

    // Draw connector lines (match → next match)
    nodes.forEach((node) => {
      if (node.match.nextMatchId) {
        const target = nodeMap.get(node.match.nextMatchId);
        if (target) {
          const x1 = node.x + this.NODE_W;
          const y1 = node.y + this.NODE_H / 2;
          const x2 = target.x;
          const y2 = target.y + this.NODE_H / 2;
          const mx = (x1 + x2) / 2;
          g.append('path')
            .attr('d', `M${x1},${y1} C${mx},${y1} ${mx},${y2} ${x2},${y2}`)
            .attr('fill', 'none')
            .attr('stroke', '#ccc')
            .attr('stroke-width', 2);
        }
      }
    });

    // Draw match nodes
    const matchClickEmitter = this.matchClick;
    const nodeG = g
      .selectAll('.match-node')
      .data(nodes)
      .enter()
      .append('g')
      .attr('class', 'match-node')
      .attr('transform', (d) => `translate(${d.x},${d.y})`)
      .style('cursor', (d) =>
        d.match.status !== 'BYE' && (d.match.teamA !== null || d.match.teamB !== null)
          ? 'pointer'
          : 'default',
      )
      .on('click', (_event, d) => {
        if (d.match.status !== 'BYE' && (d.match.teamA !== null || d.match.teamB !== null)) {
          matchClickEmitter.emit(d.match.id);
        }
      });

    nodeG
      .append('rect')
      .attr('width', this.NODE_W)
      .attr('height', this.NODE_H)
      .attr('rx', 6)
      .attr('fill', (d) => (d.match.winner ? accent : '#fff'))
      .attr('stroke', (d) => (d.match.winner ? accent : primary))
      .attr('stroke-width', 2);

    // Team A label (top half)
    nodeG
      .append('text')
      .attr('x', 8)
      .attr('y', 20)
      .attr('font-size', 13)
      .attr('fill', '#222')
      .text((d) => d.match.teamA?.name ?? (d.match.status === 'BYE' ? 'BYE' : 'TBD'));

    // Team B label (bottom half)
    nodeG
      .append('text')
      .attr('x', 8)
      .attr('y', 44)
      .attr('font-size', 13)
      .attr('fill', '#222')
      .text((d) => (d.match.status === 'BYE' ? '' : (d.match.teamB?.name ?? 'TBD')));

    // Divider line
    nodeG
      .append('line')
      .attr('x1', 0)
      .attr('y1', this.NODE_H / 2)
      .attr('x2', this.NODE_W)
      .attr('y2', this.NODE_H / 2)
      .attr('stroke', '#eee')
      .attr('stroke-width', 1);

    // Winner indicator — fade-in animation via the 'winner-check' CSS class
    nodeG
      .filter((d) => d.match.winner !== null)
      .append('text')
      .attr('class', 'winner-check')
      .attr('x', this.NODE_W - 6)
      .attr('y', 14)
      .attr('text-anchor', 'end')
      .attr('font-size', 10)
      .attr('fill', '#fff')
      .text('✓');
  }

  private svgHeight(rounds: BracketResponse['rounds']): number {
    const maxMatches = Math.max(...rounds.map((r) => r.matches.length), 1);
    return maxMatches * (this.NODE_H + this.ROW_GAP) - this.ROW_GAP;
  }
}
