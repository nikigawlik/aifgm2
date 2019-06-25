Mindestanforderungen:

- Vorausschauende Bewegung (muss nicht A* sein, aber auch kein ad-hoc-Verfahren nur über Nachbarschaft)
  
- Unterschiedliche Nutzung der drei Bots (nicht alle identisch steuern, idealerweise - aber nicht zwingend - kooperativ unter Ausnutzung der einzelnen Besonderheiten)
  
- Berücksichtigung der eigenen Energie in der Strategie

Strategy / ideas:

- Bots can go anywhere
- Bots can find best tile within certain distance (rank tiles)
- Management of tasks in central planner, delegation of tasks to appropriate bots
  - possible task: refill
  - possible task: paint over
- Try to target player in front of me, or behind me if first
- Fat follows fat
- speedy follows speedy or goes to good area
- stop when chased ?
- determine short, long term/distance planning

Needs: 

- basic trigonometry
- Pathfinding
- rank function (based on closeness of tiles, and importance of task)

Cooperation stretch goals:

- lead opponents into wall (especially when chased)
- exchange information
- determine good hunting grounds