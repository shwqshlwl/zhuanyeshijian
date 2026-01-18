export function getVisitorId() {
  const key = 'ai_visitor_id'
  let vid = localStorage.getItem(key)
  if (!vid) {
    vid = 'v_' + Date.now() + Math.random().toString(36).substr(2, 9)
    localStorage.setItem(key, vid)
  }
  return vid
}
