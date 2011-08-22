//  DIM SUM 2 -- Demography and Individual Migration Simulated Using a Markov chain
//  Copyright (C) 2011 Peter Hoffmann <p-hoffmann@web.de>
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

#ifndef TIMER_H_
#define TIMER_H_
#include <ctime>
#include <string>

class Timer
{
public:
  Timer(std::string name0, bool v);

  virtual
  ~Timer();

  /**
   * Starts the timer
   */
  void
  start();

  /**
   * Stops the times
   */
  clock_t
  stop();

  /**
   * Prints the measured time
   */
  void
  print();

  static bool enabled;

private:
  std::string name;
  bool running;
  bool verbose;
  clock_t overallTime, tmpTime;
};

#endif /* TIMER_H_ */
